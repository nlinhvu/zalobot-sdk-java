package dev.linhvu.zalobot.listener;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ContainerPropertiesTests {

	@Test
	void defaultValues_areCorrect() {
		ContainerProperties props = new ContainerProperties();
		assertThat(props.getPollTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(props.getPollInterval()).isEqualTo(Duration.ofSeconds(0));
		assertThat(props.getShutdownTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(props.getBackOffInterval()).isEqualTo(Duration.ofSeconds(1));
		assertThat(props.getMaxBackOffInterval()).isEqualTo(Duration.ofSeconds(30));
	}

	@Test
	void settersAndGetters_workCorrectly() {
		ContainerProperties props = new ContainerProperties();
		props.setPollTimeout(Duration.ofSeconds(60));
		props.setPollInterval(Duration.ofMillis(500));
		props.setShutdownTimeout(Duration.ofSeconds(30));
		props.setBackOffInterval(Duration.ofMillis(2000));
		props.setMaxBackOffInterval(Duration.ofSeconds(120));

		assertThat(props.getPollTimeout()).isEqualTo(Duration.ofSeconds(60));
		assertThat(props.getPollInterval()).isEqualTo(Duration.ofMillis(500));
		assertThat(props.getShutdownTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(props.getBackOffInterval()).isEqualTo(Duration.ofMillis(2000));
		assertThat(props.getMaxBackOffInterval()).isEqualTo(Duration.ofSeconds(120));
	}

	@Test
	void defaultListenerTaskExecutor_isNull() {
		ContainerProperties props = new ContainerProperties();
		assertThat(props.getListenerTaskExecutor()).isNull();
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