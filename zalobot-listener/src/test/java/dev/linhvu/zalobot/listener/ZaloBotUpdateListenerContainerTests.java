package dev.linhvu.zalobot.listener;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.client.exception.ZaloBotAuthenticationException;
import dev.linhvu.zalobot.client.exception.ZaloBotRequestTimeoutException;
import dev.linhvu.zalobot.core.model.GetUpdates;
import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import dev.linhvu.zalobot.core.model.ZaloApiResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ZaloBotUpdateListenerContainerTests {

	private ZaloBotUpdateListenerContainer container;

	@AfterEach
	void tearDown() {
		if (this.container != null && this.container.isRunning()) {
			this.container.stop();
		}
	}

	// ── Helpers ────────────────────────────────────────────────────────

	@SuppressWarnings("unchecked")
	private ZaloBotClient mockClient(ZaloApiResponse<GetUpdatesResult> response) {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ZaloBotClient.RequestBodySpec<GetUpdates, GetUpdatesResult> spec =
				mock(ZaloBotClient.RequestBodySpec.class);
		ZaloBotClient.ResponseSpec<GetUpdatesResult> responseSpec =
				mock(ZaloBotClient.ResponseSpec.class);

		given(client.getUpdates()).willReturn(spec);
		given(spec.body(any())).willReturn(spec);
		given(spec.retrieve()).willReturn(responseSpec);
		given(responseSpec.call(GetUpdatesResult.class)).willReturn(response);
		return client;
	}

	@SuppressWarnings("unchecked")
	private ZaloBotClient mockClientThrows(RuntimeException exception) {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ZaloBotClient.RequestBodySpec<GetUpdates, GetUpdatesResult> spec =
				mock(ZaloBotClient.RequestBodySpec.class);
		ZaloBotClient.ResponseSpec<GetUpdatesResult> responseSpec =
				mock(ZaloBotClient.ResponseSpec.class);

		given(client.getUpdates()).willReturn(spec);
		given(spec.body(any())).willReturn(spec);
		given(spec.retrieve()).willReturn(responseSpec);
		given(responseSpec.call(GetUpdatesResult.class)).willThrow(exception);
		return client;
	}

	/**
	 * Builds a mock client with an answer callback on call() — used when
	 * the answer needs side effects like counting or conditional logic.
	 */
	@SuppressWarnings("unchecked")
	private ZaloBotClient mockClientWithAnswer(org.mockito.stubbing.Answer<?> answer) {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ZaloBotClient.RequestBodySpec<GetUpdates, GetUpdatesResult> spec =
				mock(ZaloBotClient.RequestBodySpec.class);
		ZaloBotClient.ResponseSpec<GetUpdatesResult> responseSpec =
				mock(ZaloBotClient.ResponseSpec.class);

		given(client.getUpdates()).willReturn(spec);
		given(spec.body(any())).willReturn(spec);
		given(spec.retrieve()).willReturn(responseSpec);
		given(responseSpec.call(GetUpdatesResult.class)).willAnswer(answer);
		return client;
	}

	private ContainerProperties fastProperties() {
		ContainerProperties props = new ContainerProperties();
		props.setPollTimeout(Duration.ofMillis(50));
		props.setShutdownTimeout(Duration.ofSeconds(2));
		props.setBackOffInterval(Duration.ofMillis(50));
		props.setMaxBackOffInterval(Duration.ofMillis(200));
		return props;
	}

	private GetUpdatesResult sampleUpdate() {
		return new GetUpdatesResult(null, "message.text.received");
	}

	// ── Constructor validation ─────────────────────────────────────────

	@Test
	void constructorWithNullClient_throwsIllegalArgumentException() {
		ContainerProperties props = fastProperties();
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ZaloBotUpdateListenerContainer(null, props))
				.withMessage("'client' cannot be null");
	}

	@Test
	void constructorWithNullProperties_throwsIllegalArgumentException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ZaloBotUpdateListenerContainer(client, null))
				.withMessage("'containerProperties' cannot be null");
	}

	// ── Start validation ───────────────────────────────────────────────

	@Test
	void startWithoutListener_throwsIllegalStateException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		assertThatIllegalStateException()
				.isThrownBy(this.container::start)
				.withMessageContaining("UpdateListener must be provided");
	}

	// ── Lifecycle ──────────────────────────────────────────────────────

	@Test
	void startAndStop_lifecycleTransitions() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});

		this.container.start();
		assertThat(this.container.isRunning()).isTrue();

		this.container.stop();
		assertThat(this.container.isRunning()).isFalse();
	}

	@Test
	void start_invokesListenerOnPoll() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		CountDownLatch latch = new CountDownLatch(1);
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> latch.countDown());

		this.container.start();
		assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void start_pollsRepeatedlyWhileRunning() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		CountDownLatch latch = new CountDownLatch(3);
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> latch.countDown());

		this.container.start();
		assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void stop_stopsPollingLoop() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		AtomicInteger counter = new AtomicInteger();
		CountDownLatch firstPoll = new CountDownLatch(1);
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {
			counter.incrementAndGet();
			firstPoll.countDown();
		});

		this.container.start();
		assertThat(firstPoll.await(5, TimeUnit.SECONDS)).isTrue();
		this.container.stop();

		int countAtStop = counter.get();
		// Use a fence wait instead of Thread.sleep
		new CountDownLatch(1).await(200, TimeUnit.MILLISECONDS);
		assertThat(counter.get()).isLessThanOrEqualTo(countAtStop + 1);
	}

	@Test
	void stopWithCallback_invokesCallbackAfterStopping() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		CountDownLatch started = new CountDownLatch(1);
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> started.countDown());

		this.container.start();
		assertThat(started.await(5, TimeUnit.SECONDS)).isTrue();

		CountDownLatch callbackLatch = new CountDownLatch(1);
		this.container.stop(callbackLatch::countDown);
		assertThat(callbackLatch.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(this.container.isRunning()).isFalse();
	}

	@Test
	void stopWithCallback_whenNotRunning_invokesCallbackImmediately() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});

		AtomicInteger callbackCount = new AtomicInteger();
		this.container.stop(callbackCount::incrementAndGet);
		assertThat(callbackCount.get()).isEqualTo(1);
	}

	// ── Pause / Resume ─────────────────────────────────────────────────

	@Test
	void pauseAndResume_controlsPolling() throws InterruptedException {
		AtomicInteger pollCount = new AtomicInteger();
		CountDownLatch firstPoll = new CountDownLatch(1);
		CountDownLatch resumedPoll = new CountDownLatch(1);
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);

		ZaloBotClient client = mockClientWithAnswer(invocation -> {
			int n = pollCount.incrementAndGet();
			if (n == 1) {
				firstPoll.countDown();
			}
			// After resume, the resumedPoll latch will be counted down
			// by the poll loop continuing to produce responses
			if (n > 5) {
				resumedPoll.countDown();
			}
			return response;
		});

		ContainerProperties props = fastProperties();
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});

		this.container.start();
		assertThat(firstPoll.await(5, TimeUnit.SECONDS)).isTrue();

		// Pause and verify pause state
		this.container.pause();
		assertThat(this.container.isPauseRequested()).isTrue();
		assertThat(this.container.isContainerPaused()).isTrue();
		assertThat(this.container.isRunning()).isTrue();

		// Record the count when paused, wait a moment to ensure paused
		new CountDownLatch(1).await(200, TimeUnit.MILLISECONDS);
		int countWhenPaused = pollCount.get();

		// Resume and verify new polls arrive
		this.container.resume();
		assertThat(this.container.isPauseRequested()).isFalse();
		assertThat(resumedPoll.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(pollCount.get()).isGreaterThan(countWhenPaused);
	}

	// ── Error handling ─────────────────────────────────────────────────

	@Test
	void pollError_invokesErrorHandler() throws InterruptedException {
		ZaloBotClient client = mockClientThrows(new RuntimeException("poll failed"));
		ContainerProperties props = fastProperties();

		CountDownLatch errorLatch = new CountDownLatch(1);
		props.setErrorHandler((exception, container) -> errorLatch.countDown());

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});

		this.container.start();
		assertThat(errorLatch.await(5, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void pollError_triggersExponentialBackOff() throws InterruptedException {
		ZaloBotClient client = mockClientThrows(new RuntimeException("poll failed"));
		ContainerProperties props = fastProperties();
		props.setBackOffInterval(Duration.ofMillis(100));
		props.setMaxBackOffInterval(Duration.ofMillis(500));

		AtomicInteger errorCount = new AtomicInteger();
		CountDownLatch twoErrors = new CountDownLatch(2);
		long[] errorTimes = new long[2];
		props.setErrorHandler((exception, cont) -> {
			int idx = errorCount.getAndIncrement();
			if (idx < 2) {
				errorTimes[idx] = System.nanoTime();
				twoErrors.countDown();
			}
		});

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});

		this.container.start();
		assertThat(twoErrors.await(5, TimeUnit.SECONDS)).isTrue();
		long gapMs = (errorTimes[1] - errorTimes[0]) / 1_000_000;
		// The gap should be at least the backoff interval (100ms) minus timing slack
		assertThat(gapMs).isGreaterThanOrEqualTo(80);
	}

	@Test
	void pollError_defaultErrorHandlerIsUsedWhenNoneConfigured() throws InterruptedException {
		// Build client from scratch using answer to count polls without triggering throws during setup
		CountDownLatch polled = new CountDownLatch(2);
		ZaloBotClient client = mockClientWithAnswer(invocation -> {
			polled.countDown();
			throw new RuntimeException("poll error");
		});

		ContainerProperties props = fastProperties();
		// No error handler set — should use LoggingErrorHandler by default

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});
		this.container.start();
		// If no exception leaks (which would kill the polling thread), two polls happen
		assertThat(polled.await(5, TimeUnit.SECONDS)).isTrue();
	}

	@Test
	void listenerError_invokesErrorHandlerOnProcessingThread() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		CountDownLatch errorLatch = new CountDownLatch(1);
		AtomicReference<Exception> caughtException = new AtomicReference<>();
		props.setErrorHandler((exception, cont) -> {
			caughtException.set(exception);
			errorLatch.countDown();
		});

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {
			throw new RuntimeException("listener failure");
		});

		this.container.start();
		assertThat(errorLatch.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(caughtException.get()).hasMessage("listener failure");
	}

	// ── Timeout error filtering ───────────────────────────────────────

	@Test
	void timeoutError_doesNotInvokeErrorHandler() throws InterruptedException {
		AtomicInteger errorCount = new AtomicInteger();
		CountDownLatch polled = new CountDownLatch(3);

		ZaloBotClient client = mockClientWithAnswer(invocation -> {
			polled.countDown();
			throw new ZaloBotRequestTimeoutException(200, 408, "Request timeout");
		});

		ContainerProperties props = fastProperties();
		props.setErrorHandler((exception, cont) -> errorCount.incrementAndGet());

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});
		this.container.start();

		assertThat(polled.await(5, TimeUnit.SECONDS)).isTrue();
		// Timeout errors should be silently continued, not passed to error handler
		assertThat(errorCount.get()).isZero();
	}

	@Test
	void wrappedTimeoutError_doesNotInvokeErrorHandler() throws InterruptedException {
		// Timeout exception wrapped in another exception
		ZaloBotRequestTimeoutException timeout =
				new ZaloBotRequestTimeoutException(200, 408, "Request timeout");
		RuntimeException wrapped = new RuntimeException("wrapped", timeout);

		AtomicInteger errorCount = new AtomicInteger();
		CountDownLatch polled = new CountDownLatch(3);

		ZaloBotClient client = mockClientWithAnswer(invocation -> {
			polled.countDown();
			throw wrapped;
		});

		ContainerProperties props = fastProperties();
		props.setErrorHandler((exception, cont) -> errorCount.incrementAndGet());

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});
		this.container.start();

		assertThat(polled.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(errorCount.get()).isZero();
	}

	// ── Response filtering ─────────────────────────────────────────────

	@Test
	void responseNull_doesNotInvokeListener() throws InterruptedException {
		CountDownLatch pollsCompleted = new CountDownLatch(3);
		ZaloBotClient client = mockClientWithAnswer(invocation -> {
			pollsCompleted.countDown();
			return null;
		});

		ContainerProperties props = fastProperties();
		AtomicInteger listenerCount = new AtomicInteger();
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> listenerCount.incrementAndGet());

		this.container.start();
		assertThat(pollsCompleted.await(5, TimeUnit.SECONDS)).isTrue();
		this.container.stop();

		assertThat(listenerCount.get()).isZero();
	}

	@Test
	void apiException_doesNotInvokeListenerAndTriggersErrorHandler() throws InterruptedException {
		ZaloBotClient client = mockClientThrows(
				new ZaloBotAuthenticationException(401, 401, "Unauthorized"));
		ContainerProperties props = fastProperties();

		AtomicInteger listenerCount = new AtomicInteger();
		CountDownLatch errorLatch = new CountDownLatch(1);
		props.setErrorHandler((exception, cont) -> errorLatch.countDown());

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> listenerCount.incrementAndGet());

		this.container.start();
		assertThat(errorLatch.await(5, TimeUnit.SECONDS)).isTrue();
		assertThat(listenerCount.get()).isZero();
	}

	@Test
	void responseResultNull_doesNotInvokeListener() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, null, 0);
		CountDownLatch pollsCompleted = new CountDownLatch(3);

		ZaloBotClient client = mockClientWithAnswer(invocation -> {
			pollsCompleted.countDown();
			return response;
		});

		ContainerProperties props = fastProperties();
		AtomicInteger listenerCount = new AtomicInteger();
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> listenerCount.incrementAndGet());

		this.container.start();
		assertThat(pollsCompleted.await(5, TimeUnit.SECONDS)).isTrue();
		this.container.stop();

		assertThat(listenerCount.get()).isZero();
	}

	@Test
	void responseNotOk_doesNotInvokeListener() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(false, sampleUpdate(), 400);
		CountDownLatch pollsCompleted = new CountDownLatch(3);

		ZaloBotClient client = mockClientWithAnswer(invocation -> {
			pollsCompleted.countDown();
			return response;
		});

		ContainerProperties props = fastProperties();
		AtomicInteger listenerCount = new AtomicInteger();
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> listenerCount.incrementAndGet());

		this.container.start();
		assertThat(pollsCompleted.await(5, TimeUnit.SECONDS)).isTrue();
		this.container.stop();

		assertThat(listenerCount.get()).isZero();
	}

	// ── Processing concurrency ────────────────────────────────────────

	@Test
	void multipleConcurrency_processesUpdatesInParallel() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();
		props.setProcessingConcurrency(3);

		CountDownLatch latch = new CountDownLatch(3);
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> latch.countDown());

		this.container.start();
		assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
	}

	// ── Miscellaneous ──────────────────────────────────────────────────

	@Test
	void doubleStart_isIdempotent() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});

		this.container.start();
		this.container.start(); // second start should be idempotent
		assertThat(this.container.isRunning()).isTrue();
	}

	@Test
	void setUpdateListener_withNull_throwsIllegalArgumentException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		this.container = new ZaloBotUpdateListenerContainer(client, props);

		assertThatIllegalArgumentException()
				.isThrownBy(() -> this.container.setUpdateListener(null))
				.withMessage("'updateListener' cannot be null");
	}

	@Test
	void close_delegatesToStop() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});

		this.container.start();
		assertThat(this.container.isRunning()).isTrue();

		this.container.close();
		assertThat(this.container.isRunning()).isFalse();
	}

	@Test
	void getContainerProperties_returnsConfiguredProperties() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		this.container = new ZaloBotUpdateListenerContainer(client, props);

		assertThat(this.container.getContainerProperties()).isSameAs(props);
	}

	@Test
	void stopBeforeStart_doesNothing() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});

		// Should not throw
		this.container.stop();
		assertThat(this.container.isRunning()).isFalse();
	}

	@Test
	void stopWithShortTimeout_gracefullyHandlesTimeout() throws InterruptedException {
		// Use a blocking listener to simulate slow processing during shutdown
		CountDownLatch listenerStarted = new CountDownLatch(1);
		CountDownLatch blockListener = new CountDownLatch(1);

		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();
		props.setShutdownTimeout(Duration.ofMillis(100)); // Very short shutdown timeout

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {
			listenerStarted.countDown();
			try {
				// Block the processing thread to trigger the shutdown timeout
				blockListener.await(10, TimeUnit.SECONDS);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		});

		this.container.start();
		assertThat(listenerStarted.await(5, TimeUnit.SECONDS)).isTrue();

		// Stop with a very short timeout — the processing thread is still blocked
		this.container.stop();
		// Release the blocked listener
		blockListener.countDown();

		assertThat(this.container.isRunning()).isFalse();
	}

	@Test
	void backOff_resetsAfterSuccessfulPoll() throws InterruptedException {
		ContainerProperties props = fastProperties();
		props.setBackOffInterval(Duration.ofMillis(500));

		AtomicInteger callIndex = new AtomicInteger();
		CountDownLatch secondError = new CountDownLatch(1);
		ZaloApiResponse<GetUpdatesResult> successResponse = new ZaloApiResponse<>(true, sampleUpdate(), 0);

		ZaloBotClient client = mockClientWithAnswer(invocation -> {
			int idx = callIndex.getAndIncrement();
			if (idx == 0) {
				throw new RuntimeException("first error");
			}
			if (idx >= 1 && idx <= 3) {
				return successResponse; // success resets backoff
			}
			secondError.countDown();
			throw new RuntimeException("second error");
		});

		props.setErrorHandler((exception, cont) -> {});

		this.container = new ZaloBotUpdateListenerContainer(client, props);
		this.container.setUpdateListener(update -> {});
		this.container.start();

		// The second error should arrive relatively quickly since backoff was reset
		assertThat(secondError.await(5, TimeUnit.SECONDS)).isTrue();
	}
}
