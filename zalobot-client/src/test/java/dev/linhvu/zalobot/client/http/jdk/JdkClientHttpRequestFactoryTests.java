package dev.linhvu.zalobot.client.http.jdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.HttpMethod;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JdkClientHttpRequestFactoryTests {

	@Test
	void defaultConstructor_createsFactory() {
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		assertThat(factory).isNotNull();
	}

	@Test
	void constructorWithHttpClient_createsFactory() {
		HttpClient httpClient = HttpClient.newHttpClient();
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
		assertThat(factory).isNotNull();
	}

	@Test
	void createRequest_returnsRequestWithCorrectUriAndMethod() {
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		URI uri = URI.create("https://example.com/test");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);

		assertThat(request).isNotNull();
		assertThat(request.getURI()).isEqualTo(uri);
		assertThat(request.getMethod()).isEqualTo(HttpMethod.POST);
	}

	@Test
	void setReadTimeout_withMillis() {
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		factory.setReadTimeout(5000);
		// Verify it doesn't throw and creates requests
		ClientHttpRequest request = factory.createRequest(URI.create("https://example.com"), HttpMethod.POST);
		assertThat(request).isNotNull();
	}

	@Test
	void setReadTimeout_withDuration() {
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		factory.setReadTimeout(Duration.ofSeconds(5));
		ClientHttpRequest request = factory.createRequest(URI.create("https://example.com"), HttpMethod.POST);
		assertThat(request).isNotNull();
	}
}
