package dev.linhvu.zalobot.client.http.jdk;

import java.io.ByteArrayOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.linhvu.zalobot.client.exception.ZaloBotClientException;
import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.ClientHttpResponse;
import dev.linhvu.zalobot.client.http.HttpMethod;

/**
 * {@link ClientHttpRequest} implementation backed by the JDK 11+ {@link HttpClient}.
 *
 * <p>Sends requests asynchronously and supports configurable read timeouts
 * via an internal {@code TimeoutHandler}.
 *
 * @author Linh Vu
 * @since 0.0.1
 */
class JdkClientHttpRequest implements ClientHttpRequest {

	private final HttpClient httpClient;
	private final HttpMethod method;
	private final URI uri;
	private final Map<String, String> headers = new LinkedHashMap<>();
	private ByteArrayOutputStream body = new ByteArrayOutputStream(1024);
	private final Duration readTimeout;

	/**
	 * Creates a new JDK-backed HTTP request.
	 *
	 * @param httpClient the JDK HTTP client to use for sending
	 * @param uri the request URI
	 * @param method the HTTP method
	 * @param readTimeout the read timeout duration, or {@code null} for no timeout
	 */
	JdkClientHttpRequest(HttpClient httpClient, URI uri, HttpMethod method, Duration readTimeout) {
		this.httpClient = httpClient;
		this.uri = uri;
		this.method = method;
		this.readTimeout = readTimeout;
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
	public ClientHttpResponse execute() throws IOException {
		CompletableFuture<HttpResponse<InputStream>> responseFuture = null;
		TimeoutHandler timeoutHandler = null;
		try {
			HttpRequest request = buildRequest();
			responseFuture = this.httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofInputStream());
			if (readTimeout != null) {
				timeoutHandler = new TimeoutHandler(responseFuture, this.readTimeout);
				HttpResponse<InputStream> response = responseFuture.get();
				InputStream inputStream = timeoutHandler.wrapInputStream(response);
				return new JdkClientHttpResponse(response, inputStream);
			}
			else {
				HttpResponse<InputStream> response = responseFuture.get();
				return new JdkClientHttpResponse(response, response.body());
			}
		}
		catch (InterruptedException e) {
			throw new ZaloBotClientException("HTTP request interrupted", e);
		}
		catch (ExecutionException e) {
			Throwable cause = e.getCause();
			if (cause instanceof CancellationException ce) {
				if (timeoutHandler != null) {
					timeoutHandler.handleCancellationException(ce);
				}
				throw new IOException("Request cancelled", cause);
			}
			throw new ZaloBotClientException("HTTP request failed: " + e.getMessage(), e);
		}
		catch (CancellationException ex) {
			if (timeoutHandler != null) {
				timeoutHandler.handleCancellationException(ex);
			}
			throw new IOException("Request cancelled", ex);
		}
	}

	private HttpRequest buildRequest() {
		HttpRequest.Builder builder = HttpRequest.newBuilder().uri(this.uri);

		this.headers.forEach(builder::header);

		byte[] bodyBytes = this.body.toByteArray();
		if (bodyBytes.length > 0) {
			builder.method(this.method.name(), HttpRequest.BodyPublishers.ofByteArray(bodyBytes));
		} else {
			builder.method(this.method.name(), HttpRequest.BodyPublishers.noBody());
		}

		return builder.build();
	}

	/** Manages read-timeout cancellation for an in-flight async HTTP request. */
	private final class TimeoutHandler {

		private final CompletableFuture<Void> timeoutFuture;
		private final AtomicBoolean timeout = new AtomicBoolean(false);

		private TimeoutHandler(CompletableFuture<HttpResponse<InputStream>> future, Duration timeout) {
			this.timeoutFuture = new CompletableFuture<Void>()
					.completeOnTimeout(null, timeout.toMillis(), TimeUnit.MILLISECONDS);

			this.timeoutFuture.thenRun(() -> {
				this.timeout.set(true);
				if (future.cancel(true) || future.isCompletedExceptionally() || !future.isDone()) {
					return;
				}
				try {
					future.get().body().close();
				}
				catch (Exception e) {
					// ignore
				}
			});
		}

		public InputStream wrapInputStream(HttpResponse<InputStream> response) {
			InputStream body = response.body();
			if (body == null) {
				return body;
			}
			return new FilterInputStream(body) {

				@Override
				public void close() throws IOException {
					TimeoutHandler.this.timeoutFuture.cancel(false);
					super.close();
				}
			};
		}

		public void handleCancellationException(CancellationException ex) throws HttpTimeoutException {
			if (this.timeout.get()) {
				throw new HttpTimeoutException(ex.getMessage());
			}
		}
	}

}
