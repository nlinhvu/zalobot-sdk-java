package dev.linhvu.zalobot.listener;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.client.exception.ZaloBotAuthenticationException;
import dev.linhvu.zalobot.core.model.GetUpdates;
import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import dev.linhvu.zalobot.core.model.ZaloApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ZaloUpdateListenerContainerTests {

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
				.isThrownBy(() -> new ZaloUpdateListenerContainer(null, props))
				.withMessage("'client' cannot be null");
	}

	@Test
	void constructorWithNullProperties_throwsIllegalArgumentException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ZaloUpdateListenerContainer(client, null))
				.withMessage("'containerProperties' cannot be null");
	}

	// ── Start validation ───────────────────────────────────────────────

	@Test
	void startWithoutListener_throwsIllegalStateException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		// No updateListener set
		assertThatIllegalStateException()
				.isThrownBy(container::start)
				.withMessageContaining("UpdateListener must be provided");
	}

	// ── Lifecycle ──────────────────────────────────────────────────────

	@Test
	void startAndStop_lifecycleTransitions() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();
		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> {});

		container.start();
		assertThat(container.isRunning()).isTrue();

		container.stop();
		assertThat(container.isRunning()).isFalse();
	}

	@Test
	void start_invokesListenerOnPoll() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		CountDownLatch latch = new CountDownLatch(1);
		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> latch.countDown());

		container.start();
		try {
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}
		finally {
			container.stop();
		}
	}

	@Test
	void start_pollsRepeatedlyWhileRunning() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		CountDownLatch latch = new CountDownLatch(3);
		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> latch.countDown());

		container.start();
		try {
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
		}
		finally {
			container.stop();
		}
	}

	@Test
	void stop_stopsPollingLoop() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		AtomicInteger counter = new AtomicInteger();
		CountDownLatch firstPoll = new CountDownLatch(1);
		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> {
			counter.incrementAndGet();
			firstPoll.countDown();
		});

		container.start();
		assertThat(firstPoll.await(5, TimeUnit.SECONDS)).isTrue();
		container.stop();

		int countAtStop = counter.get();
		Thread.sleep(200);
		// Counter should not have increased significantly after stop
		assertThat(counter.get()).isLessThanOrEqualTo(countAtStop + 1);
	}

	// ── Pause / Resume ─────────────────────────────────────────────────

	// ── Error handling ─────────────────────────────────────────────────

	@Test
	void pollError_invokesErrorHandler() throws InterruptedException {
		ZaloBotClient client = mockClientThrows(new RuntimeException("poll failed"));
		ContainerProperties props = fastProperties();

		CountDownLatch errorLatch = new CountDownLatch(1);
		props.setErrorHandler((exception, container) -> errorLatch.countDown());

		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> {});

		container.start();
		try {
			assertThat(errorLatch.await(5, TimeUnit.SECONDS)).isTrue();
		}
		finally {
			container.stop();
		}
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
				errorTimes[idx] = System.currentTimeMillis();
				twoErrors.countDown();
			}
		});

		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> {});

		container.start();
		try {
			assertThat(twoErrors.await(5, TimeUnit.SECONDS)).isTrue();
			long gap = errorTimes[1] - errorTimes[0];
			// The gap should be at least the backoff interval (100ms)
			assertThat(gap).isGreaterThanOrEqualTo(80); // allow some timing slack
		}
		finally {
			container.stop();
		}
	}

	// ── Response filtering ─────────────────────────────────────────────

	@Test
	void responseNull_doesNotInvokeListener() throws InterruptedException {
		ZaloBotClient client = mockClient(null);
		ContainerProperties props = fastProperties();

		AtomicInteger listenerCount = new AtomicInteger();
		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> listenerCount.incrementAndGet());

		container.start();
		Thread.sleep(200);
		container.stop();

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

		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> listenerCount.incrementAndGet());

		container.start();
		try {
			assertThat(errorLatch.await(5, TimeUnit.SECONDS)).isTrue();
			assertThat(listenerCount.get()).isZero();
		}
		finally {
			container.stop();
		}
	}

	@Test
	void responseResultNull_doesNotInvokeListener() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, null, 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		AtomicInteger listenerCount = new AtomicInteger();
		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> listenerCount.incrementAndGet());

		container.start();
		Thread.sleep(200);
		container.stop();

		assertThat(listenerCount.get()).isZero();
	}

	// ── Miscellaneous ──────────────────────────────────────────────────

	@Test
	void doubleStart_isIdempotent() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> {});

		container.start();
		container.start(); // second start should be idempotent
		assertThat(container.isRunning()).isTrue();
		container.stop();
	}

	@Test
	void setUpdateListener_withNull_throwsIllegalArgumentException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);

		assertThatIllegalArgumentException()
				.isThrownBy(() -> container.setUpdateListener(null))
				.withMessage("'updateListener' cannot be null");
	}

	@Test
	void close_delegatesToStop() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ZaloUpdateListenerContainer container = new ZaloUpdateListenerContainer(client, props);
		container.setUpdateListener(update -> {});

		container.start();
		assertThat(container.isRunning()).isTrue();

		container.close(); // should delegate to stop()
		assertThat(container.isRunning()).isFalse();
	}
}