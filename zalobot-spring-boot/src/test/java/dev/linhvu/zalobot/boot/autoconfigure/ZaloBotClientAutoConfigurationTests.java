package dev.linhvu.zalobot.boot.autoconfigure;

import dev.linhvu.zalobot.boot.ZaloBotClientCustomizer;
import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.client.ZaloBotUrl;
import org.junit.jupiter.api.Test;

import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import static org.assertj.core.api.Assertions.*;

class ZaloBotClientAutoConfigurationTests {

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
			.withConfiguration(AutoConfigurations.of(ZaloBotClientAutoConfiguration.class));

	@Test
	void whenTokenIsPresent_thenClientBeanCreated() {
		this.contextRunner
				.withPropertyValues("zalobot.bot-token=test-token")
				.run(context -> {
					assertThat(context).hasSingleBean(ZaloBotClient.class);
					assertThat(context).hasSingleBean(ZaloBotClient.Builder.class);
				});
	}

	@Test
	void whenTokenIsMissing_thenNoBeanCreated() {
		this.contextRunner
				.run(context -> {
					assertThat(context).doesNotHaveBean(ZaloBotClient.class);
					assertThat(context).doesNotHaveBean(ZaloBotClient.Builder.class);
				});
	}

	@Test
	void whenUserDefinesOwnClient_thenAutoConfigBacksAway() {
		ZaloBotClient customClient = ZaloBotClient.botToken("custom-token");

		this.contextRunner
				.withPropertyValues("zalobot.bot-token=test-token")
				.withBean(ZaloBotClient.class, () -> customClient)    // user-defined bean
				.run(context -> {
					assertThat(context).hasSingleBean(ZaloBotClient.class);
					assertThat(context.getBean(ZaloBotClient.class)).isSameAs(customClient);
				});
	}

	@Test
	void whenCustomPropertiesSet_thenBoundCorrectly() {
		this.contextRunner
				.withPropertyValues(
						"zalobot.bot-token=test-token",
						"zalobot.client.host=custom-api.example.com",
						"zalobot.client.port=8443",
						"zalobot.listener.poll-timeout=60s",
						"zalobot.listener.concurrency=3")
				.run(context -> {
					ZaloBotProperties props = context.getBean(ZaloBotProperties.class);
					assertThat(props.getBotToken()).isEqualTo("test-token");
					assertThat(props.getClient().getHost()).isEqualTo("custom-api.example.com");
					assertThat(props.getClient().getPort()).isEqualTo(8443);
					assertThat(props.getListener().getPollTimeout())
							.isEqualTo(java.time.Duration.ofSeconds(60));
					assertThat(props.getListener().getConcurrency()).isEqualTo(3);
				});
	}

	@Test
	void whenCustomizerBeanPresent_thenCustomizerApplied() {
		this.contextRunner
				.withPropertyValues("zalobot.bot-token=test-token")
				.withBean(ZaloBotClientCustomizer.class, () -> builder ->
						builder.zaloBotUrl(new ZaloBotUrl("http", "custom-host", 9090)))
				.run(context -> {
					assertThat(context).hasSingleBean(ZaloBotClient.class);
					assertThat(context).hasSingleBean(ZaloBotClient.Builder.class);
				});
	}

	@Test
	void builderBean_isPrototypeScoped() {
		this.contextRunner
				.withPropertyValues("zalobot.bot-token=test-token")
				.run(context -> {
					ZaloBotClient.Builder builder1 = context.getBean(ZaloBotClient.Builder.class);
					ZaloBotClient.Builder builder2 = context.getBean(ZaloBotClient.Builder.class);
					assertThat(builder1).isNotSameAs(builder2);
				});
	}
}