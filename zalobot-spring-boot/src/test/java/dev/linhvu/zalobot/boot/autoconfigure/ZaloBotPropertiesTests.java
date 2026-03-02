package dev.linhvu.zalobot.boot.autoconfigure;

import java.time.Duration;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ZaloBotPropertiesTests {

	// ── Root properties ───────────────────────────────────────────────

	@Test
	void botToken_defaultIsNull() {
		ZaloBotProperties props = new ZaloBotProperties();
		assertThat(props.getBotToken()).isNull();
	}

	@Test
	void botToken_setAndGet() {
		ZaloBotProperties props = new ZaloBotProperties();
		props.setBotToken("my-token");
		assertThat(props.getBotToken()).isEqualTo("my-token");
	}

	@Test
	void client_isNotNull() {
		ZaloBotProperties props = new ZaloBotProperties();
		assertThat(props.getClient()).isNotNull();
	}

	@Test
	void listener_isNotNull() {
		ZaloBotProperties props = new ZaloBotProperties();
		assertThat(props.getListener()).isNotNull();
	}

	// ── Client defaults ───────────────────────────────────────────────

	@Test
	void clientDefaults_areCorrect() {
		ZaloBotProperties.Client client = new ZaloBotProperties().getClient();
		assertThat(client.getScheme()).isEqualTo("https");
		assertThat(client.getHost()).isEqualTo("bot-api.zaloplatforms.com");
		assertThat(client.getPort()).isEqualTo(443);
	}

	@Test
	void clientSetters_workCorrectly() {
		ZaloBotProperties.Client client = new ZaloBotProperties().getClient();
		client.setScheme("http");
		client.setHost("localhost");
		client.setPort(9090);

		assertThat(client.getScheme()).isEqualTo("http");
		assertThat(client.getHost()).isEqualTo("localhost");
		assertThat(client.getPort()).isEqualTo(9090);
	}

	// ── Listener defaults ─────────────────────────────────────────────

	@Test
	void listenerDefaults_areCorrect() {
		ZaloBotProperties.Listener listener = new ZaloBotProperties().getListener();
		assertThat(listener.isEnabled()).isTrue();
		assertThat(listener.getPollTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(listener.getShutdownTimeout()).isEqualTo(Duration.ofSeconds(10));
		assertThat(listener.getBackOffInterval()).isEqualTo(Duration.ofSeconds(1));
		assertThat(listener.getMaxBackOffInterval()).isEqualTo(Duration.ofSeconds(30));
		assertThat(listener.getQueueCapacity()).isEqualTo(64);
		assertThat(listener.getProcessingConcurrency()).isEqualTo(1);
		assertThat(listener.isObservationEnabled()).isTrue();
	}

	@Test
	void listenerSetters_workCorrectly() {
		ZaloBotProperties.Listener listener = new ZaloBotProperties().getListener();
		listener.setEnabled(false);
		listener.setPollTimeout(Duration.ofSeconds(60));
		listener.setShutdownTimeout(Duration.ofSeconds(30));
		listener.setBackOffInterval(Duration.ofSeconds(2));
		listener.setMaxBackOffInterval(Duration.ofSeconds(120));
		listener.setQueueCapacity(128);
		listener.setProcessingConcurrency(4);
		listener.setObservationEnabled(false);

		assertThat(listener.isEnabled()).isFalse();
		assertThat(listener.getPollTimeout()).isEqualTo(Duration.ofSeconds(60));
		assertThat(listener.getShutdownTimeout()).isEqualTo(Duration.ofSeconds(30));
		assertThat(listener.getBackOffInterval()).isEqualTo(Duration.ofSeconds(2));
		assertThat(listener.getMaxBackOffInterval()).isEqualTo(Duration.ofSeconds(120));
		assertThat(listener.getQueueCapacity()).isEqualTo(128);
		assertThat(listener.getProcessingConcurrency()).isEqualTo(4);
		assertThat(listener.isObservationEnabled()).isFalse();
	}
}
