package dev.linhvu.zalobot.boot.autoconfigure;

import dev.linhvu.zalobot.listener.UpdateListenerContainer;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ZaloBotListenerContainerLifecycleTests {
	private final UpdateListenerContainer container = mock(UpdateListenerContainer.class);
	private final ZaloBotListenerContainerLifecycle lifecycle =
			new ZaloBotListenerContainerLifecycle(container);

	@Test
	void start_delegatesToContainer() {
		lifecycle.start();
		verify(container).start();
	}

	@Test
	void stop_delegatesToContainer() {
		lifecycle.stop();
		verify(container).stop();
	}

	@Test
	void isRunning_delegatesToContainer() {
		given(container.isRunning()).willReturn(true);
		assertThat(lifecycle.isRunning()).isTrue();

		given(container.isRunning()).willReturn(false);
		assertThat(lifecycle.isRunning()).isFalse();
	}

	@Test
	void isAutoStartup_returnsTrue() {
		assertThat(lifecycle.isAutoStartup()).isTrue();
	}

	@Test
	void getPhase_returnsIntegerMaxValue() {
		assertThat(lifecycle.getPhase()).isEqualTo(Integer.MAX_VALUE);
	}
}