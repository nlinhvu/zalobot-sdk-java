package dev.linhvu.zalobot.boot.autoconfigure;

import dev.linhvu.zalobot.listener.ContainerProperties;
import dev.linhvu.zalobot.listener.ErrorHandler;
import dev.linhvu.zalobot.listener.UpdateListener;
import dev.linhvu.zalobot.listener.UpdateListenerContainer;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class ZaloBotListenerAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(
					ZaloBotClientAutoConfiguration.class,
					ZaloBotListenerAutoConfiguration.class));

	@Test
	void whenListenerBeanPresent_thenContainerCreated() {
		this.contextRunner
				.withPropertyValues("zalobot.bot-token=test-token")
				.withBean(UpdateListener.class, () -> update -> {})    // user's listener
				.run(context -> {
					assertThat(context).hasSingleBean(UpdateListenerContainer.class);
					assertThat(context).hasSingleBean(ZaloBotListenerContainerLifecycle.class);
					assertThat(context).hasSingleBean(ContainerProperties.class);
				});
	}

	@Test
	void whenNoListenerBean_thenNoContainerCreated() {
		this.contextRunner
				.withPropertyValues("zalobot.bot-token=test-token")
				// no UpdateListener bean
				.run(context -> {
					assertThat(context).doesNotHaveBean(UpdateListenerContainer.class);
					assertThat(context).doesNotHaveBean(ZaloBotListenerContainerLifecycle.class);
				});
	}

	@Test
	void whenListenerDisabled_thenNoContainerCreated() {
		this.contextRunner
				.withPropertyValues(
						"zalobot.bot-token=test-token",
						"zalobot.listener.enabled=false")
				.withBean(UpdateListener.class, () -> update -> {})
				.run(context -> {
					assertThat(context).doesNotHaveBean(UpdateListenerContainer.class);
				});
	}

	@Test
	void whenCustomErrorHandlerPresent_thenUsedByContainer() {
		ErrorHandler customHandler = (exception, container) -> {};

		this.contextRunner
				.withPropertyValues("zalobot.bot-token=test-token")
				.withBean(UpdateListener.class, () -> update -> {})
				.withBean(ErrorHandler.class, () -> customHandler)
				.run(context -> {
					assertThat(context).hasSingleBean(ContainerProperties.class);
					ContainerProperties cp = context.getBean(ContainerProperties.class);
					assertThat(cp.getErrorHandler()).isSameAs(customHandler);
				});
	}

	@Test
	void containerPropertiesValues_matchApplicationProperties() {
		this.contextRunner
				.withPropertyValues(
						"zalobot.bot-token=test-token",
						"zalobot.listener.poll-timeout=60s",
						"zalobot.listener.shutdown-timeout=30s",
						"zalobot.listener.back-off-interval=2s",
						"zalobot.listener.max-back-off-interval=120s",
						"zalobot.listener.queue-capacity=128",
						"zalobot.listener.processing-concurrency=4")
				.withBean(UpdateListener.class, () -> update -> {})
				.run(context -> {
					ContainerProperties cp = context.getBean(ContainerProperties.class);
					assertThat(cp.getPollTimeout()).isEqualTo(java.time.Duration.ofSeconds(60));
					assertThat(cp.getShutdownTimeout()).isEqualTo(java.time.Duration.ofSeconds(30));
					assertThat(cp.getBackOffInterval()).isEqualTo(java.time.Duration.ofSeconds(2));
					assertThat(cp.getMaxBackOffInterval()).isEqualTo(java.time.Duration.ofSeconds(120));
					assertThat(cp.getQueueCapacity()).isEqualTo(128);
					assertThat(cp.getProcessingConcurrency()).isEqualTo(4);
				});
	}
}