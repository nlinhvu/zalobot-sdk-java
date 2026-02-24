package dev.linhvu.zalobot.client.http.okhhtp3;

import java.net.URI;

import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.ClientHttpRequestFactory;
import dev.linhvu.zalobot.client.http.HttpMethod;
import okhttp3.OkHttpClient;

/**
 * {@link ClientHttpRequestFactory} implementation backed by OkHttp3.
 *
 * <p>If no {@link OkHttpClient} is provided, a default instance is created.
 */
public class OkClientHttpRequestFactory implements ClientHttpRequestFactory {

	private final OkHttpClient httpClient;

	/**
	 * Creates a factory using the given {@link OkHttpClient}.
	 *
	 * @param httpClient the OkHttp client to use
	 */
	public OkClientHttpRequestFactory(OkHttpClient httpClient) {
		this.httpClient = httpClient;
	}

	/**
	 * Creates a factory with a default {@link OkHttpClient}.
	 */
	public OkClientHttpRequestFactory() {
		this(new OkHttpClient());
	}

	@Override
	public ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod) {
		return new OkClientHttpRequest(this.httpClient, uri, httpMethod);
	}
}
