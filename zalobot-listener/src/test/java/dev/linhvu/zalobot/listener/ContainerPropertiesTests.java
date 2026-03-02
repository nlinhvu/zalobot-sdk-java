package dev.linhvu.zalobot.listener;

import java.time.Duration;

import dev.linhvu.zalobot.listener.observation.ZaloBotListenerObservationConvention;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.mock;

class ContainerPropertiesTests {

	@Test
	void defaultValues_areCorrect() {
		ContainerProperties props = new ContainerProperties();
		assertThat(props.getPollTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(props.getShutdownTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(props.getBackOffInterval()).isEqualTo(Duration.ofSeconds(1));
		assertThat(props.getMaxBackOffInterval()).isEqualTo(Duration.ofSeconds(30));
		assertThat(props.getQueueCapacity()).isEqualTo(64);
		assertThat(props.getProcessingConcurrency()).isEqualTo(1);
	}

	@Test
	void settersAndGetters_workCorrectly() {
		ContainerProperties props = new ContainerProperties();
		props.setPollTimeout(Duration.ofSeconds(60));
		props.setShutdownTimeout(Duration.ofSeconds(30));
		props.setBackOffInterval(Duration.ofMillis(2000));
		props.setMaxBackOffInterval(Duration.ofSeconds(120));
		props.setQueueCapacity(128);
		props.setProcessingConcurrency(4);

		assertThat(props.getPollTimeout()).isEqualTo(Duration.ofSeconds(60));
		assertThat(props.getShutdownTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(props.getBackOffInterval()).isEqualTo(Duration.ofMillis(2000));
		assertThat(props.getMaxBackOffInterval()).isEqualTo(Duration.ofSeconds(120));
		assertThat(props.getQueueCapacity()).isEqualTo(128);
		assertThat(props.getProcessingConcurrency()).isEqualTo(4);
	}

	@Test
	void setQueueCapacity_withZeroOrNegative_throwsIllegalArgumentException() {
		ContainerProperties props = new ContainerProperties();
		assertThatIllegalArgumentException()
				.isThrownBy(() -> props.setQueueCapacity(0));
		assertThatIllegalArgumentException()
				.isThrownBy(() -> props.setQueueCapacity(-1));
	}

	@Test
	void setProcessingConcurrency_withZeroOrNegative_throwsIllegalArgumentException() {
		ContainerProperties props = new ContainerProperties();
		assertThatIllegalArgumentException()
				.isThrownBy(() -> props.setProcessingConcurrency(0));
		assertThatIllegalArgumentException()
				.isThrownBy(() -> props.setProcessingConcurrency(-1));
	}

	@Test
	void defaultUpdateListener_isNull() {
		ContainerProperties props = new ContainerProperties();
		assertThat(props.getUpdateListener()).isNull();
	}

	@Test
	void defaultErrorHandler_isNull() {
		ContainerProperties props = new ContainerProperties();
		assertThat(props.getErrorHandler()).isNull();
	}

	@Test
	void updateListener_setAndGet() {
		ContainerProperties props = new ContainerProperties();
		UpdateListener listener = update -> {};
		props.setUpdateListener(listener);
		assertThat(props.getUpdateListener()).isSameAs(listener);
	}

	@Test
	void errorHandler_setAndGet() {
		ContainerProperties props = new ContainerProperties();
		ErrorHandler handler = (exception, container) -> {};
		props.setErrorHandler(handler);
		assertThat(props.getErrorHandler()).isSameAs(handler);
	}

	@Test
	void defaultObservationRegistry_isNoop() {
		ContainerProperties props = new ContainerProperties();
		assertThat(props.getObservationRegistry()).isSameAs(ObservationRegistry.NOOP);
	}

	@Test
	void setObservationRegistry_withNonNull() {
		ContainerProperties props = new ContainerProperties();
		ObservationRegistry registry = mock(ObservationRegistry.class);
		props.setObservationRegistry(registry);
		assertThat(props.getObservationRegistry()).isSameAs(registry);
	}

	@Test
	void setObservationRegistry_withNull_throwsException() {
		ContainerProperties props = new ContainerProperties();
		assertThatIllegalArgumentException()
				.isThrownBy(() -> props.setObservationRegistry(null));
	}

	@Test
	void defaultObservationConvention_isNull() {
		ContainerProperties props = new ContainerProperties();
		assertThat(props.getObservationConvention()).isNull();
	}

	@Test
	void setObservationConvention_setAndGet() {
		ContainerProperties props = new ContainerProperties();
		ZaloBotListenerObservationConvention convention =
				mock(ZaloBotListenerObservationConvention.class);
		props.setObservationConvention(convention);
		assertThat(props.getObservationConvention()).isSameAs(convention);
	}
}
