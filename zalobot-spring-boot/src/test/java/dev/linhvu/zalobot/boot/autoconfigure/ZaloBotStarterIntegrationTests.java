package dev.linhvu.zalobot.boot.autoconfigure;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.listener.UpdateListener;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest(properties = "zalobot.bot-token=integration-test-token")
class ZaloBotStarterIntegrationTests {

	@Autowired
	private ZaloBotClient client;

	@Autowired
	private ZaloBotProperties properties;

	@Test
	void contextLoads() {
		assertThat(this.client).isNotNull();
		assertThat(this.properties.getBotToken()).isEqualTo("integration-test-token");
	}

	@SpringBootApplication
	static class TestApplication {

		@Bean
		UpdateListener updateListener() {
			return update -> {
				// test listener — does nothing
			};
		}
	}
}
