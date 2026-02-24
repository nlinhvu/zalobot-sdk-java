package dev.linhvu.zalobot.client.http.okhhtp3;

import java.io.IOException;
import java.io.InputStream;

import dev.linhvu.zalobot.client.http.ClientHttpResponse;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * {@link ClientHttpResponse} implementation that wraps an OkHttp3 {@link Response}.
 */
public class OkClientHttpResponse implements ClientHttpResponse {

	private final Response response;

	/**
	 * Creates a response wrapping the given OkHttp response.
	 *
	 * @param response the OkHttp response
	 */
	public OkClientHttpResponse(Response response) {
		this.response = response;
	}

	@Override
	public int getStatusCode() {
		return this.response.code();
	}

	@Override
	public InputStream getBody() {
		ResponseBody body = this.response.body();
		return body != null ? body.byteStream() : InputStream.nullInputStream();
	}

	@Override
	public void close() throws IOException {
		this.response.close();
	}
}
