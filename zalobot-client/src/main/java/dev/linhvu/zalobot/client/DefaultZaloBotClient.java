package dev.linhvu.zalobot.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.linhvu.zalobot.client.exception.ZaloBotApiException;
import dev.linhvu.zalobot.client.exception.ZaloBotAuthenticationException;
import dev.linhvu.zalobot.client.exception.ZaloBotClientException;
import dev.linhvu.zalobot.client.exception.ZaloBotException;
import dev.linhvu.zalobot.client.exception.ZaloBotRequestTimeoutException;
import dev.linhvu.zalobot.client.exception.ZaloBotSerializationException;
import dev.linhvu.zalobot.client.exception.ZaloErrorCode;
import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.ClientHttpRequestFactory;
import dev.linhvu.zalobot.client.http.ClientHttpResponse;
import dev.linhvu.zalobot.client.http.HttpMethod;
import dev.linhvu.zalobot.core.model.GetMe;
import dev.linhvu.zalobot.core.model.GetMeResult;
import dev.linhvu.zalobot.core.model.GetUpdates;
import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import dev.linhvu.zalobot.core.model.SendChatAction;
import dev.linhvu.zalobot.core.model.SendMessage;
import dev.linhvu.zalobot.core.model.SendMessageResult;
import dev.linhvu.zalobot.core.model.SendPhoto;
import dev.linhvu.zalobot.core.model.SendSticker;
import dev.linhvu.zalobot.core.model.ZaloApiResponse;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

/**
 * Default implementation of {@link ZaloBotClient}.
 *
 * <p>Handles URI construction, JSON serialization/deserialization, HTTP request
 * execution, and error mapping. Uses a fluent request/response pipeline through
 * nested spec classes.
 *
 * <p>Instances are created via {@link ZaloBotClient#builder()}.
 */
final class DefaultZaloBotClient implements ZaloBotClient {

	private final ZaloBotUrl url;
	private final String botToken;
	private final ClientHttpRequestFactory clientHttpRequestFactory;
	private final JsonMapper jsonMapper;
	private final DefaultZaloBotClientBuilder builder;

	public DefaultZaloBotClient(ZaloBotUrl url,
			String botToken,
			ClientHttpRequestFactory clientHttpRequestFactory,
			JsonMapper jsonMapper,
			DefaultZaloBotClientBuilder builder) {
		this.url = url;
		this.botToken = botToken;
		this.clientHttpRequestFactory = clientHttpRequestFactory;
		this.jsonMapper = jsonMapper;
		this.builder = builder;
	}

	/** Returns a new builder pre-populated with this client's configuration. */
	public Builder mutate() {
		return new DefaultZaloBotClientBuilder(this.builder);
	}


	@Override
	public RequestBodySpec<GetMe, GetMeResult> getMe() {
		return new DefaultRequestBodySpec<>(HttpMethod.POST, "getMe");
	}

	@Override
	public RequestBodySpec<GetUpdates, GetUpdatesResult> getUpdates() {
		return new DefaultRequestBodySpec<>(HttpMethod.POST, "getUpdates");
	}

	@Override
	public RequestBodySpec<SendMessage, SendMessageResult> sendMessage() {
		return new DefaultRequestBodySpec<>(HttpMethod.POST, "sendMessage");
	}

	@Override
	public RequestBodySpec<SendPhoto, SendMessageResult> sendPhoto() {
		return new DefaultRequestBodySpec<>(HttpMethod.POST, "sendPhoto");
	}

	@Override
	public RequestBodySpec<SendSticker, SendMessageResult> sendSticker() {
		return new DefaultRequestBodySpec<>(HttpMethod.POST, "sendSticker");
	}

	@Override
	public RequestBodySpec<SendChatAction, SendMessageResult> sendChatAction() {
		return new DefaultRequestBodySpec<>(HttpMethod.POST, "sendChatAction");
	}

	/** Builds the full API URI for the given method path. */
	private URI buildUri(String methodPath) {
		try {
			return new URI(
					this.url.scheme(),
					null,
					this.url.host(),
					this.url.port(),
					"/bot" + this.botToken + "/" + methodPath,
					null,
					null);
		}
		catch (URISyntaxException e) {
			throw new ZaloBotClientException("Invalid URI: " + e.getMessage(), e);
		}
	}

	/** Executes an HTTP request and deserializes the API response, mapping errors to exceptions. */
	private <N> ZaloApiResponse<N> exchangeInternal(HttpMethod method, String methodPath, Map<String, String> headers, Object body, Class<N> clazz) {

		URI uri = buildUri(methodPath);

		ClientHttpRequest request = this.clientHttpRequestFactory.createRequest(uri, method);

		Map<String, String> requestHeaders = request.getHeaders();
		requestHeaders.putAll(headers);

		if (body != null) {
			try {
				byte[] bodyBytes = this.jsonMapper.writeValueAsBytes(body);
				OutputStream outputStream = request.getBody();
				outputStream.write(bodyBytes);
				outputStream.flush();
			}
			catch (IOException e) {
				throw new ZaloBotSerializationException("Failed to serialize request body", e);
			}
		}

		try (ClientHttpResponse response = request.execute()) {
			int httpStatus = response.getStatusCode();
			JavaType javaType = this.jsonMapper.getTypeFactory().constructParametricType(ZaloApiResponse.class, clazz);
			ZaloApiResponse<N> apiResponse = this.jsonMapper.readValue(response.getBody(), javaType);
			if (apiResponse != null && !apiResponse.ok()) {
				ZaloErrorCode code = ZaloErrorCode.fromCode(apiResponse.errorCode());
				String description = code.getDescription();
				if (code.isAuthenticationError()) {
					throw new ZaloBotAuthenticationException(httpStatus, apiResponse.errorCode(), description);
				}
				if (code.isRequestTimeout()) {
					throw new ZaloBotRequestTimeoutException(httpStatus, apiResponse.errorCode(), description);
				}
				throw new ZaloBotApiException(httpStatus, apiResponse.errorCode(), description);
			}
			return apiResponse;
		}
		catch (ZaloBotException e) {
			throw e;
		}
		catch (IOException e) {
			throw new ZaloBotClientException("HTTP request failed: " + e.getMessage(), e);
		}
	}

	/** Default {@link RequestBodySpec} that accumulates headers and body for a request. */
	private class DefaultRequestBodySpec<M, N> implements RequestBodySpec<M, N> {

		private final HttpMethod method;
		private final String methodPath;
		private final Map<String, String> headers = new LinkedHashMap<>();
		private M body;

		DefaultRequestBodySpec(HttpMethod method, String methodPath) {
			this.method = method;
			this.methodPath = methodPath;
			this.headers.put("Content-Type", "application/json");
		}

		@Override
		public RequestBodySpec<M, N> header(String headerName, String headerValue) {
			this.headers.put(headerName, headerValue);
			return this;
		}

		@Override
		public RequestBodySpec<M, N> body(M body) {
			this.body = body;
			return this;
		}

		@Override
		public ResponseSpec<N> retrieve() {
			return new DefaultResponseSpec<>(this);
		}
	}

	/** Default {@link ResponseSpec} that delegates to {@link #exchangeInternal}. */
	private class DefaultResponseSpec<N> implements ResponseSpec<N> {

		private final DefaultRequestBodySpec<?, N> requestSpec;

		private DefaultResponseSpec(DefaultRequestBodySpec<?, N> requestSpec) {
			this.requestSpec = requestSpec;
		}

		@Override
		public ZaloApiResponse<N> call(Class<N> clazz) {
			return exchangeInternal(this.requestSpec.method,
					this.requestSpec.methodPath,
					this.requestSpec.headers,
					this.requestSpec.body,
					clazz);
		}
	}

}
