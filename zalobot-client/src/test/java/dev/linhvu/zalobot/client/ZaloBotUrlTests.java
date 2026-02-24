package dev.linhvu.zalobot.client;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ZaloBotUrlTests {

	@Test
	void defaultUrl_hasCorrectValues() {
		ZaloBotUrl url = ZaloBotUrl.DEFAULT_URL;
		assertThat(url.scheme()).isEqualTo("https");
		assertThat(url.host()).isEqualTo("bot-api.zaloplatforms.com");
		assertThat(url.port()).isEqualTo(443);
	}

	@Test
	void customUrl_recordAccessors() {
		ZaloBotUrl url = new ZaloBotUrl("http", "localhost", 8080);
		assertThat(url.scheme()).isEqualTo("http");
		assertThat(url.host()).isEqualTo("localhost");
		assertThat(url.port()).isEqualTo(8080);
	}

	@Test
	void equalsAndHashCode() {
		ZaloBotUrl a = new ZaloBotUrl("https", "example.com", 443);
		ZaloBotUrl b = new ZaloBotUrl("https", "example.com", 443);
		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

}