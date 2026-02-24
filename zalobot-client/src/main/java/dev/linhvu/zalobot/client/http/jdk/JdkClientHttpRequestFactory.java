package dev.linhvu.zalobot.client.http.jdk;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.ClientHttpRequestFactory;
import dev.linhvu.zalobot.client.http.HttpMethod;

/**
 * {@link ClientHttpRequestFactory} implementation backed by the JDK 11+
 * {@link HttpClient}.
 *
 * <p>Supports configurable read timeouts. If no {@link HttpClient} is provided,
 * a default instance is created.
 */
public class JdkClientHttpRequestFactory implements ClientHttpRequestFactory {

	private final HttpClient httpClient;

	private Duration readTimeout;

	/**
	 * Creates a factory using the given {@link HttpClient}.
	 *
	 * @param httpClient the JDK HTTP client to use
	 */
	public JdkClientHttpRequestFactory(HttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Creates a factory with a default {@link HttpClient}.
	 */
	public JdkClientHttpRequestFactory() {
		this(HttpClient.newHttpClient());
	}

	/**
	 * Sets the read timeout in milliseconds.
	 *
	 * @param readTimeout the timeout in milliseconds
	 */
	public void setReadTimeout(int readTimeout) {
		this.readTimeout = Duration.ofMillis(readTimeout);
	}

	/**
	 * Sets the read timeout as a {@link Duration}.
	 *
	 * @param readTimeout the timeout duration
	 */
	public void setReadTimeout(Duration readTimeout) {
		this.readTimeout = readTimeout;
	}

	@Override
	public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) {
		return new JdkClientHttpRequest(this.httpClient, uri, httpMethod, this.readTimeout);
	}
}
