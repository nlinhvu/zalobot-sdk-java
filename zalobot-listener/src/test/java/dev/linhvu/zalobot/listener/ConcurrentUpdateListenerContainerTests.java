package dev.linhvu.zalobot.listener;

import java.time.Duration;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.core.model.GetUpdates;
import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import dev.linhvu.zalobot.core.model.ZaloApiResponse;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

class ConcurrentUpdateListenerContainerTests {
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

	private ContainerProperties fastProperties() {
		ContainerProperties props = new ContainerProperties();
		props.setPollTimeout(Duration.ofMillis(50));
		props.setPollInterval(Duration.ofMillis(10));
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
				.isThrownBy(() -> new ConcurrentUpdateListenerContainer(null, props))
				.withMessage("'client' cannot be null");
	}

	@Test
	void constructorWithNullProperties_throwsIllegalArgumentException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ConcurrentUpdateListenerContainer(client, null))
				.withMessage("'containerProperties' cannot be null");
	}

	// ── Concurrency ────────────────────────────────────────────────────

	@Test
	void defaultConcurrency_isOne() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		assertThat(container.getConcurrency()).isEqualTo(1);
	}

	@Test
	void setConcurrency_withZero_throwsIllegalArgumentException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		assertThatIllegalArgumentException()
				.isThrownBy(() -> container.setConcurrency(0))
				.withMessage("concurrency must be > 0");
	}

	@Test
	void setConcurrency_withNegative_throwsIllegalArgumentException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		assertThatIllegalArgumentException()
				.isThrownBy(() -> container.setConcurrency(-1))
				.withMessage("concurrency must be > 0");
	}

	// ── Start / Stop ───────────────────────────────────────────────────

	@Test
	void start_createsCorrectNumberOfChildren() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		container.setConcurrency(3);
		container.setUpdateListener(update -> {});

		container.start();
		try {
			assertThat(container.getContainers()).hasSize(3);
		}
		finally {
			container.stop();
		}
	}

	@Test
	void start_allChildrenRunning() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		container.setConcurrency(2);
		container.setUpdateListener(update -> {});

		container.start();
		try {
			assertThat(container.getContainers()).allMatch(ZaloUpdateListenerContainer::isRunning);
		}
		finally {
			container.stop();
		}
	}

	@Test
	void stop_stopsAllChildren() {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		container.setConcurrency(2);
		container.setUpdateListener(update -> {});

		container.start();
		container.stop();

		assertThat(container.isRunning()).isFalse();
		assertThat(container.getContainers()).isEmpty();
	}

	// ── Pause / Resume ─────────────────────────────────────────────────

	@Test
	void pause_pausesAllChildren() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		container.setConcurrency(2);
		container.setUpdateListener(update -> {});

		container.start();
		Thread.sleep(100); // let children start
		container.pause();

		try {
			assertThat(container.isPauseRequested()).isTrue();
			assertThat(container.getContainers()).allMatch(AbstractUpdateListenerContainer::isPauseRequested);
		}
		finally {
			container.stop();
		}
	}

	@Test
	void resume_resumesAllChildren() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		container.setConcurrency(2);
		container.setUpdateListener(update -> {});

		container.start();
		Thread.sleep(100);
		container.pause();
		container.resume();

		try {
			assertThat(container.isPauseRequested()).isFalse();
			assertThat(container.getContainers()).noneMatch(AbstractUpdateListenerContainer::isPauseRequested);
		}
		finally {
			container.stop();
		}
	}

	@Test
	void isContainerPaused_onlyTrueWhenAllChildrenPaused() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		container.setConcurrency(2);
		container.setUpdateListener(update -> {});

		container.start();
		Thread.sleep(100);

		assertThat(container.isContainerPaused()).isFalse();

		container.pause();
		// Wait for all children to actually pause
		boolean paused = false;
		for (int i = 0; i < 50; i++) {
			if (container.isContainerPaused()) {
				paused = true;
				break;
			}
			Thread.sleep(50);
		}
		assertThat(paused).isTrue();
		container.stop();
	}

	// ── Multi-threaded ─────────────────────────────────────────────────

	@Test
	void concurrentListeners_receiveOnDifferentThreads() throws InterruptedException {
		ZaloApiResponse<GetUpdatesResult> response = new ZaloApiResponse<>(true, sampleUpdate(), 0);
		ZaloBotClient client = mockClient(response);
		ContainerProperties props = fastProperties();

		ConcurrentSkipListSet<String> threadNames = new ConcurrentSkipListSet<>();
		CountDownLatch latch = new CountDownLatch(4); // at least 4 invocations across 2 threads

		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		container.setConcurrency(2);
		container.setUpdateListener(update -> {
			threadNames.add(Thread.currentThread().getName());
			latch.countDown();
		});

		container.start();
		try {
			assertThat(latch.await(5, TimeUnit.SECONDS)).isTrue();
			assertThat(threadNames).hasSizeGreaterThanOrEqualTo(2);
		}
		finally {
			container.stop();
		}
	}

	// ── toString ───────────────────────────────────────────────────────

	@Test
	void toString_includesStateInfo() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);
		container.setConcurrency(3);

		String str = container.toString();
		assertThat(str).contains("ConcurrentUpdateListenerContainer");
		assertThat(str).contains("concurrency=3");
		assertThat(str).contains("running=false");
	}

	// ── Start without listener ─────────────────────────────────────────

	@Test
	void startWithoutListener_throwsIllegalStateException() {
		ZaloBotClient client = mock(ZaloBotClient.class);
		ContainerProperties props = fastProperties();
		ConcurrentUpdateListenerContainer container = new ConcurrentUpdateListenerContainer(client, props);

		assertThatIllegalStateException()
				.isThrownBy(container::start)
				.withMessageContaining("UpdateListener must be provided");
	}
}