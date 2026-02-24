package dev.linhvu.zalobot.client;

import dev.linhvu.zalobot.client.http.ClientHttpRequestFactory;
import dev.linhvu.zalobot.client.http.jdk.JdkClientHttpRequestFactory;
import dev.linhvu.zalobot.client.http.okhhtp3.OkClientHttpRequestFactory;
import dev.linhvu.zalobot.client.util.Assert;
import dev.linhvu.zalobot.client.util.ClassUtils;
import tools.jackson.databind.json.JsonMapper;

/**
 * Default implementation of {@link ZaloBotClient.Builder}.
 *
 * <p>Auto-detects the available HTTP client library at runtime with the following
 * fallback chain: OkHttp3 &rarr; JDK HttpClient (Java 11+). If neither is available,
 * an {@link IllegalStateException} is thrown at build time.
 */
final class DefaultZaloBotClientBuilder implements ZaloBotClient.Builder {

	private static final boolean JDK_CLIENT_PRESENT;
	private static final boolean OKHTTP3_CLIENT_PRESENT;

	static {
		ClassLoader loader = DefaultZaloBotClientBuilder.class.getClassLoader();

		JDK_CLIENT_PRESENT = ClassUtils.isPresent("java.net.http.HttpClient", loader);
		OKHTTP3_CLIENT_PRESENT = ClassUtils.isPresent("okhttp3.OkHttpClient", loader);
	}


	private ZaloBotUrl url;
	private String botToken;
	private ClientHttpRequestFactory requestFactory;
	private JsonMapper jsonMapper;

	public DefaultZaloBotClientBuilder() {
	}

	public DefaultZaloBotClientBuilder(DefaultZaloBotClientBuilder other) {
		this.url = other.url;
		this.botToken = other.botToken;
		this.requestFactory = other.requestFactory;
		this.jsonMapper = other.jsonMapper;
	}

	@Override
	public ZaloBotClient.Builder zaloBotUrl(ZaloBotUrl url) {
		this.url = url;
		return this;
	}

	@Override
	public ZaloBotClient.Builder botToken(String botToken) {
		this.botToken = botToken;
		return this;
	}

	@Override
	public ZaloBotClient.Builder requestFactory(ClientHttpRequestFactory requestFactory) {
		this.requestFactory = requestFactory;
		return this;
	}

	@Override
	public ZaloBotClient.Builder jsonMapper(JsonMapper jsonMapper) {
		this.jsonMapper = jsonMapper;
		return this;
	}

	@Override
	public ZaloBotClient build() {
		Assert.notNull(this.botToken, "botToken must not be null");

		ZaloBotUrl url = initUrl();
		ClientHttpRequestFactory requestFactory = initRequestFactory();
		JsonMapper jsonMapper = initJsonMapper();

		return new DefaultZaloBotClient(
				url,
				this.botToken,
				requestFactory,
				jsonMapper,
				new DefaultZaloBotClientBuilder(this));
	}

	private ZaloBotUrl initUrl() {
		if (this.url != null) {
			return this.url;
		}
		return ZaloBotUrl.DEFAULT_URL;
	}

	private ClientHttpRequestFactory initRequestFactory() {
		if (this.requestFactory != null) {
			return this.requestFactory;
		}
		if (OKHTTP3_CLIENT_PRESENT) {
			return OkHttpClientFactoryHelper.create();
		}
		if (JDK_CLIENT_PRESENT) {
			return new JdkClientHttpRequestFactory();
		}
		throw new IllegalStateException(
				"No suitable HTTP client found. Add OkHttp or use JDK 11+ HttpClient.");
	}

	private JsonMapper initJsonMapper() {
		if (this.jsonMapper != null) {
			return this.jsonMapper;
		}
		return JsonMapper.builder().build();
	}

	/**
	 * Inner helper to avoid class-loading OkHttp when it's absent.
	 */
	private static class OkHttpClientFactoryHelper {
		static ClientHttpRequestFactory create() {
			return new OkClientHttpRequestFactory();
		}
	}
}
