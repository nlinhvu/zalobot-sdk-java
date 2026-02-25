package dev.linhvu.zalobot.listener;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

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
}