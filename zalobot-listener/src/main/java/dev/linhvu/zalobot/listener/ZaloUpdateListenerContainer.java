package dev.linhvu.zalobot.listener;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.core.model.GetUpdates;
import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import dev.linhvu.zalobot.core.model.ZaloApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single-threaded {@link UpdateListenerContainer} that continuously polls the
 * Zalo Bot API for updates using long-polling.
 *
 * <p>The polling loop runs in a dedicated thread managed by a configurable
 * {@link java.util.concurrent.Executor}. On errors, it applies exponential
 * backoff before retrying. Supports pause/resume for temporarily suspending
 * polling without stopping the container.
 *
 * @see ConcurrentUpdateListenerContainer
 */
public class ZaloUpdateListenerContainer extends AbstractUpdateListenerContainer {

	private static final Logger logger = LoggerFactory.getLogger(ZaloUpdateListenerContainer.class);

	private final ZaloBotClient client;
	private final AbstractUpdateListenerContainer thisOrParentContainer;
	private volatile ListenerConsumer listenerConsumer;
	private volatile CompletableFuture<Void> listenerConsumerFuture;
	private volatile CountDownLatch startLatch = new CountDownLatch(1);

	public ZaloUpdateListenerContainer(ZaloBotClient client, ContainerProperties containerProperties) {
		this(null, client, containerProperties);
	}

	ZaloUpdateListenerContainer(AbstractUpdateListenerContainer parent, ZaloBotClient client, ContainerProperties containerProperties) {
		super(containerProperties);
		if (client == null) {
			throw new IllegalArgumentException("'client' cannot be null");
		}
		this.client = client;
		this.thisOrParentContainer = (parent != null) ? parent : this;
	}

	@Override
	protected void doStart() {
		if (isRunning()) {
			return;
		}

		ContainerProperties properties = getContainerProperties();
		UpdateListener listener = properties.getUpdateListener();

		Executor executor = properties.getListenerTaskExecutor();
		if (executor == null) {
			executor = Executors.newSingleThreadExecutor(r -> {
				Thread thread = new Thread(r, "zalo-listener-consumer");
				thread.setDaemon(true);
				return thread;
			});
			properties.setListenerTaskExecutor(executor);
		}

		this.listenerConsumer = new ListenerConsumer(listener);
		setRunning(true);
		this.startLatch = new CountDownLatch(1);

		CompletableFuture<Void> future = new CompletableFuture<>();
		executor.execute(() -> {
			try {
				this.listenerConsumer.run();
				future.complete(null);
			} catch (Throwable t) {
				future.completeExceptionally(t);
			}
		});
		this.listenerConsumerFuture = future;

		try {
			if (!this.startLatch.await(5, TimeUnit.SECONDS)) {
				logger.warn("Consumer thread failed to start within 5 seconds");
			}
		}
		catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	@Override
	protected void doStop(Runnable callback) {
		if (isRunning()) {
			setRunning(false);
			if (this.listenerConsumerFuture != null) {
				this.listenerConsumerFuture.whenComplete((result, ex) -> callback.run());
			}
			else {
				callback.run();
			}
		}
	}

	@Override
	public boolean isContainerPaused() {
		return isPauseRequested() && this.listenerConsumer != null
				&& this.listenerConsumer.consumerPaused;
	}

	private final class ListenerConsumer implements Runnable {

		private final UpdateListener listener;
		private volatile boolean consumerPaused = false;

		ListenerConsumer(UpdateListener listener) {
			this.listener = listener;
		}

		@Override
		public void run() {
			ZaloUpdateListenerContainer.this.startLatch.countDown();

			ContainerProperties properties = getContainerProperties();
			Duration pollInterval = properties.getPollInterval();
			Duration pollTimeout = properties.getPollTimeout();

			ErrorHandler errorHandler = properties.getErrorHandler();
			if (errorHandler == null) {
				errorHandler = new LoggingErrorHandler();
			}

			ExponentialBackOff backOff = new ExponentialBackOff(
					properties.getBackOffInterval(),
					properties.getMaxBackOffInterval());

			while (isRunning()) {
				try {
					if (isPauseRequested()) {
						if (!this.consumerPaused) {
							this.consumerPaused = true;
						}
						TimeUnit.MILLISECONDS.sleep(pollInterval.toMillis());
						continue;
					}
					else if (this.consumerPaused) {
						this.consumerPaused = false;
					}

					pollAndInvoke(pollTimeout);
					backOff.reset();
					TimeUnit.MILLISECONDS.sleep(pollInterval.toMillis());
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
				catch (Exception e) {
					errorHandler.handleError(e, ZaloUpdateListenerContainer.this);

					long backOffMillis = backOff.nextBackOffMillis();
					try {
						TimeUnit.MILLISECONDS.sleep(backOffMillis);
					}
					catch (InterruptedException ie) {
						Thread.currentThread().interrupt();
						break;
					}
				}
			}
		}

		private void pollAndInvoke(Duration pollTimeout) {
			ZaloApiResponse<GetUpdatesResult> response = ZaloUpdateListenerContainer.this.client
					.getUpdates()
					.body(new GetUpdates(pollTimeout.toSeconds()))
					.retrieve()
					.call(GetUpdatesResult.class);

			if (response != null && response.ok() && response.result() != null) {
				this.listener.onUpdate(response.result());
			}
		}
	}
}
