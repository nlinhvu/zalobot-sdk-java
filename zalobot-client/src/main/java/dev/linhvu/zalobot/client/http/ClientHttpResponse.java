package dev.linhvu.zalobot.client.http;

import java.io.Closeable;
import java.io.InputStream;

/**
 * Abstraction for an HTTP response, providing access to the status code
 * and response body.
 *
 * <p>Implements {@link Closeable} to ensure the underlying connection
 * resources are released after the response is consumed.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ClientHttpRequest#execute()
 */
public interface ClientHttpResponse extends Closeable {

	/**
	 * Returns the HTTP status code of the response.
	 *
	 * @return the status code (e.g., 200, 404)
	 */
	int getStatusCode();

	/**
	 * Returns the response body as an input stream.
	 *
	 * @return the body input stream, never {@code null}
	 */
	InputStream getBody();
}
