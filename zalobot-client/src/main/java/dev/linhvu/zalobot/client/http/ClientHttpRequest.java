package dev.linhvu.zalobot.client.http;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.Map;

/**
 * Abstraction for an HTTP request that can be executed to produce a
 * {@link ClientHttpResponse}.
 *
 * <p>Implementations buffer the request body and headers, then execute the
 * actual HTTP call when {@link #execute()} is invoked.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ClientHttpRequestFactory
 * @see ClientHttpResponse
 */
public interface ClientHttpRequest {

	/**
	 * Returns the HTTP method of this request.
	 *
	 * @return the HTTP method
	 */
	HttpMethod getMethod();

	/**
	 * Returns the URI of this request.
	 *
	 * @return the request URI
	 */
	URI getURI();

	/**
	 * Returns the mutable headers map for this request.
	 *
	 * @return the headers map
	 */
	Map<String, String> getHeaders();

	/**
	 * Returns the output stream for writing the request body.
	 *
	 * @return the body output stream
	 */
	OutputStream getBody();

	/**
	 * Executes this request and returns the response.
	 *
	 * @return the HTTP response
	 * @throws IOException if an I/O error occurs during execution
	 */
	ClientHttpResponse execute() throws IOException;
}
