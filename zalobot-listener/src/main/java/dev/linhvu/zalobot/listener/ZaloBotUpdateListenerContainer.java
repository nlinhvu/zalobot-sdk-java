package dev.linhvu.zalobot.listener;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.client.exception.ZaloBotRequestTimeoutException;
import dev.linhvu.zalobot.core.model.GetUpdates;
import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import dev.linhvu.zalobot.core.model.ZaloApiResponse;
import dev.linhvu.zalobot.listener.observation.ZaloBotListenerContext;
import dev.linhvu.zalobot.listener.observation.ZaloBotListenerObservation;
import dev.linhvu.zalobot.listener.observation.ZaloBotListenerObservationConvention;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link UpdateListenerContainer} implementation that polls the Zalo Bot API
 * using a dedicated polling thread and dispatches received updates to the
 * configured {@link UpdateListener} via a bounded {@link java.util.concurrent.BlockingQueue}.
 *
 * <p>The container uses a producer–consumer architecture:
 * <ul>
 *   <li>A single polling thread issues {@code getUpdates} calls and enqueues results.</li>
 *   <li>One or more processing threads dequeue and dispatch updates to the listener.</li>
 * </ul>
 *
 * <p>Error handling is delegated to the configured {@link ErrorHandler}, with
 * automatic exponential backoff on consecutive poll failures.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ContainerProperties#getProcessingConcurrency()
 * @see ContainerProperties#getQueueCapacity()
 */
public class ZaloBotUpdateListenerContainer extends AbstractUpdateListenerContainer {

	private static final Logger logger = LoggerFactory.getLogger(ZaloBotUpdateListenerContainer.class);

	private final ZaloBotClient client;

	private volatile CountDownLatch startLatch = new CountDownLatch(1);
	private BlockingQueue<GetUpdatesResult> updateQueue;
	private ExecutorService pollingExecutor;
	private ExecutorService processingExecutor;
	private CompletableFuture<Void> pollingFuture;
	private List<CompletableFuture<Void>> processingFutures;

	public ZaloBotUpdateListenerContainer(ZaloBotClient client, ContainerProperties containerProperties) {
		super(containerProperties);
		if (client == null) {
			throw new IllegalArgumentException("'client' cannot be null");
		}
		this.client = client;
	}

	@Override
	protected void doStart() {
		ContainerProperties properties = getContainerProperties();
		UpdateListener listener = properties.getUpdateListener();
		ObservationRegistry observationRegistry = properties.getObservationRegistry();
		ZaloBotListenerObservationConvention observationConvention = properties.getObservationConvention();
		ErrorHandler errorHandler = properties.getErrorHandler();
		if (errorHandler == null) {
			errorHandler = new LoggingErrorHandler();
		}
		int concurrency = properties.getProcessingConcurrency();

		// 1. Create the shared bounded queue
		this.updateQueue = new LinkedBlockingQueue<>(properties.getQueueCapacity());

		// 2. Set running before submitting loops so they see isRunning() == true
		setRunning(true);

		// 3. Create and start the single polling thread
		this.startLatch = new CountDownLatch(1);
		this.pollingExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "zalo-poll-0");
			t.setDaemon(false);
			return t;
		});
		PollingLoop pollingLoop = new PollingLoop(this.updateQueue);
		this.pollingFuture = CompletableFuture.runAsync(pollingLoop, this.pollingExecutor);

		// 4. Create and start processing threads
		AtomicInteger threadIndex = new AtomicInteger(0);
		this.processingExecutor = Executors.newFixedThreadPool(concurrency, r -> {
			Thread t = new Thread(r, "zalo-process-" + threadIndex.getAndIncrement());
			t.setDaemon(false);
			return t;
		});
		this.processingFutures = new ArrayList<>();
		ProcessingLoop processingLoop = new ProcessingLoop(this.updateQueue, listener, errorHandler, observationRegistry, observationConvention);
		for (int i = 0; i < concurrency; i++) {
			this.processingFutures.add(CompletableFuture.runAsync(processingLoop, this.processingExecutor));
		}

		// 5. Wait for polling thread to signal readiness
		try {
			if (!startLatch.await(5, TimeUnit.SECONDS)) {
				setRunning(false);
				throw new IllegalStateException("Polling thread did not start within 5 seconds");
			}
		}
		catch (InterruptedException e) {
			setRunning(false);
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while waiting for start", e);
		}
	}

	@Override
	protected void doStop(Runnable callback) {
		// 1. Signal all loops to stop
		setRunning(false);

		// 2. Stop adding more runAsync or execute from Executor
		this.pollingExecutor.shutdown();
		this.processingExecutor.shutdown();

		// 3. Wait for polling and processing threads to run to its end
		CompletableFuture<Void> allProcessing = CompletableFuture.allOf(
				Stream.concat(Stream.of(this.pollingFuture), this.processingFutures.stream())
						.toArray(CompletableFuture[]::new)
		);
		try {
			Duration shutdownTimeout = getContainerProperties().getShutdownTimeout();
			allProcessing.get(shutdownTimeout.toMillis(), TimeUnit.MILLISECONDS);
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			this.pollingExecutor.shutdownNow();
			this.processingExecutor.shutdownNow();
		}
		catch (ExecutionException | TimeoutException e) {
			this.pollingExecutor.shutdownNow();
			this.processingExecutor.shutdownNow();
		}

		callback.run();
	}

	@Override
	public boolean isContainerPaused() {
		return isPauseRequested();
	}

	/** Runnable that continuously polls the Zalo Bot API and enqueues results. */
	private final class PollingLoop implements Runnable {

		private final BlockingQueue<GetUpdatesResult> queue;

		PollingLoop(BlockingQueue<GetUpdatesResult> queue) {
			this.queue = queue;
		}

		@Override
		public void run() {
			ContainerProperties properties = getContainerProperties();
			Duration pollTimeout = properties.getPollTimeout();
			ErrorHandler errorHandler = properties.getErrorHandler();
			if (errorHandler == null) {
				errorHandler = new LoggingErrorHandler();
			}

			ExponentialBackOff backOff = new ExponentialBackOff(
					properties.getBackOffInterval(),
					properties.getMaxBackOffInterval());

			ZaloBotUpdateListenerContainer.this.startLatch.countDown();
			while (isRunning()) {
				try {
					if (isPauseRequested()) {
						TimeUnit.MILLISECONDS.sleep(500);
						continue;
					}

					logger.debug("Start calling /getUpdates for long-polling new message.");
					ZaloApiResponse<GetUpdatesResult> response = ZaloBotUpdateListenerContainer.this.client
							.getUpdates()
							.body(new GetUpdates(pollTimeout.toSeconds()))
							.retrieve()
							.call(GetUpdatesResult.class);

					if (response != null && response.ok() && response.result() != null) {
						logger.debug("Put the message into queue.");
						this.queue.put(response.result());
					}

					backOff.reset();
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				catch (Exception e) {
					if (isTimeoutError(e)) {
						continue;
					}

					errorHandler.handleError(e, ZaloBotUpdateListenerContainer.this);
					try {
						TimeUnit.MILLISECONDS.sleep(backOff.nextBackOffMillis());
					} catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}

		private boolean isTimeoutError(Exception e) {
			Throwable cause = e;
			while (cause != null) {
				if (cause instanceof ZaloBotRequestTimeoutException) {
					return true;
				}
				cause = cause.getCause();
			}
			return false;
		}
	}

	/** Runnable that dequeues updates and dispatches them to the {@link UpdateListener}. */
	private final class ProcessingLoop implements Runnable {

		private final BlockingQueue<GetUpdatesResult> queue;
		private final UpdateListener listener;
		private final ErrorHandler errorHandler;
		private final ObservationRegistry observationRegistry;
		private final ZaloBotListenerObservationConvention observationConvention;

		private ProcessingLoop(BlockingQueue<GetUpdatesResult> queue,
				UpdateListener listener,
				ErrorHandler errorHandler,
				ObservationRegistry observationRegistry,
				ZaloBotListenerObservationConvention observationConvention) {
			this.queue = queue;
			this.listener = listener;
			this.errorHandler = errorHandler;
			this.observationRegistry = observationRegistry;
			this.observationConvention = observationConvention;
		}

		@Override
		public void run() {
			while (isRunning() || !queue.isEmpty()) {
				try {
					GetUpdatesResult update = queue.poll(1, TimeUnit.SECONDS);
					if (update != null) {
						processUpdate(update);
					}
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				catch (Exception e) {
					errorHandler.handleError(e, ZaloBotUpdateListenerContainer.this);
				}
			}
		}

		private void processUpdate(GetUpdatesResult update) {
			ZaloBotListenerContext observationContext = new ZaloBotListenerContext("default", update);
			Observation observation = ZaloBotListenerObservation.LISTENER_OBSERVATION
					.observation(
							this.observationConvention,
							ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention.INSTANCE,
							() -> observationContext,
							this.observationRegistry);
			observation.start();
			try (Observation.Scope scope = observation.openScope()) {
				this.listener.onUpdate(update);
			}
			catch (Exception e) {
				observation.error(e);
				this.errorHandler.handleError(e, ZaloBotUpdateListenerContainer.this);
			}
			finally {
				observation.stop();
			}
		}
	}
}
