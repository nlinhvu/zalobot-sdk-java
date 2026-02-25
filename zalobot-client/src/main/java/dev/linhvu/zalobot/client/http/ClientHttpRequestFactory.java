package dev.linhvu.zalobot.client.http;

import java.net.URI;

/**
 * Factory for creating {@link ClientHttpRequest} instances.
 *
 * <p>Implementations of this interface provide the HTTP transport layer.
 * The SDK includes implementations for JDK {@code HttpClient} and OkHttp3.
 *
 * @see dev.linhvu.zalobot.client.http.jdk.JdkClientHttpRequestFactory
 * @author Linh Vu
 * @since 0.0.1
 * @see dev.linhvu.zalobot.client.http.okhhtp3.OkClientHttpRequestFactory
 */
@FunctionalInterface
public interface ClientHttpRequestFactory {

	/**
	 * Creates a new HTTP request for the given URI and method.
	 *
	 * @param uri the target URI
	 * @param httpMethod the HTTP method
	 * @return a new request instance ready for header/body configuration
	 */
	ClientHttpRequest createRequest(URI uri, HttpMethod httpMethod);
}
