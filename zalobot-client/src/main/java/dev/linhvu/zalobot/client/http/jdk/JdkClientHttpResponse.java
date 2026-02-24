package dev.linhvu.zalobot.client.http.jdk;

import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpResponse;

import dev.linhvu.zalobot.client.http.ClientHttpResponse;

/**
 * {@link ClientHttpResponse} implementation that wraps a JDK {@link HttpResponse}.
 */
public class JdkClientHttpResponse implements ClientHttpResponse {

	private final HttpResponse<InputStream> response;
	private final InputStream body;

	/**
	 * Creates a response wrapping the given JDK HTTP response.
	 *
	 * @param response the JDK HTTP response
	 * @param body the response body stream, or {@code null} for an empty body
	 */
	public JdkClientHttpResponse(HttpResponse<InputStream> response, InputStream body) {
		this.response = response;
		this.body = (body != null ? body : InputStream.nullInputStream());
	}

	@Override
	public int getStatusCode() {
		return this.response.statusCode();
	}

	@Override
	public InputStream getBody() {
		return this.body;
	}

	@Override
	public void close() throws IOException {
		this.body.close();
	}

}
