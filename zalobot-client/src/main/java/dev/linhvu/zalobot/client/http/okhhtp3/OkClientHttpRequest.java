package dev.linhvu.zalobot.client.http.okhhtp3;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import dev.linhvu.zalobot.client.exception.ZaloBotClientException;
import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.ClientHttpResponse;
import dev.linhvu.zalobot.client.http.HttpMethod;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * {@link ClientHttpRequest} implementation backed by OkHttp3.
 *
 * <p>Sends requests asynchronously using OkHttp's callback mechanism and
 * bridges the result back to the synchronous {@link #execute()} contract
 * via a {@link CompletableFuture}.
 *
 * @author Linh Vu
 * @since 0.0.1
 */
class OkClientHttpRequest implements ClientHttpRequest {

	private final OkHttpClient httpClient;
	private final HttpMethod method;
	private final URI uri;
	private final Map<String, String> headers = new LinkedHashMap<>();
	private final ByteArrayOutputStream body = new ByteArrayOutputStream(1024);

	/**
	 * Creates a new OkHttp-backed HTTP request.
	 *
	 * @param httpClient the OkHttp client to use for sending
	 * @param uri the request URI
	 * @param method the HTTP method
	 */
	OkClientHttpRequest(OkHttpClient httpClient, URI uri, HttpMethod method) {
		this.httpClient = httpClient;
		this.method = method;
		this.uri = uri;
	}

	@Override
	public HttpMethod getMethod() {
		return this.method;
	}

	@Override
	public URI getURI() {
		return this.uri;
	}

	@Override
	public Map<String, String> getHeaders() {
		return this.headers;
	}

	@Override
	public OutputStream getBody() {
		return this.body;
	}

	@Override
	public ClientHttpResponse execute() {
		try {
			Request.Builder builder = new Request.Builder().url(uri.toURL());

			if (!this.headers.isEmpty()) {
				builder.headers(Headers.of(this.headers));
			}

			byte[] bodyBytes = this.body.toByteArray();
			RequestBody requestBody = null;
			if (bodyBytes.length > 0) {
				String contentType = this.headers.getOrDefault("Content-Type", "application/json");
				requestBody = RequestBody.create(bodyBytes, MediaType.parse(contentType));
			}
			builder.method(this.method.name(), requestBody);

			Request request = builder.build();

			CompletableFuture<Response> responseFuture = new CompletableFuture<>();
			this.httpClient.newCall(request).enqueue(new OkHttpClientCallback(responseFuture));

			Response response = responseFuture.get();
			return new OkClientHttpResponse(response);
		}
		catch (MalformedURLException e) {
			throw new ZaloBotClientException("Malformed URL: " + e.getMessage(), e);
		}
		catch (ExecutionException e) {
			throw new ZaloBotClientException("HTTP request failed: " + e.getMessage(), e);
		}
		catch (InterruptedException e) {
			throw new ZaloBotClientException("HTTP request interrupted", e);
		}
	}

	/** OkHttp {@link Callback} that bridges the async result to a {@link CompletableFuture}. */
	class OkHttpClientCallback implements Callback {

		private final CompletableFuture<Response> completableFuture;

		OkHttpClientCallback(CompletableFuture<Response> completableFuture) {
			this.completableFuture = completableFuture;
		}

		@Override
		public void onFailure(Call call, IOException e) {
			this.completableFuture.completeExceptionally(e);
		}

		@Override
		public void onResponse(Call call, Response response) throws IOException {
			this.completableFuture.complete(response);
		}
	}

}
