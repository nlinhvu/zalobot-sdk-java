package dev.linhvu.zalobot.client;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import dev.linhvu.zalobot.client.exception.ZaloBotApiException;
import dev.linhvu.zalobot.client.exception.ZaloBotAuthenticationException;
import dev.linhvu.zalobot.client.exception.ZaloBotClientException;
import dev.linhvu.zalobot.client.exception.ZaloErrorCode;
import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.ClientHttpRequestFactory;
import dev.linhvu.zalobot.client.http.ClientHttpResponse;
import dev.linhvu.zalobot.client.http.HttpMethod;
import dev.linhvu.zalobot.core.model.GetMeResult;
import dev.linhvu.zalobot.core.model.SendMessage;
import dev.linhvu.zalobot.core.model.SendMessageResult;
import dev.linhvu.zalobot.core.model.ZaloApiResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class DefaultZaloBotClientTests {

	// ── Helper: mock the full transport chain ──────────────────────────

	private record MockTransport(
			ClientHttpRequestFactory factory,
			ClientHttpRequest request,
			ClientHttpResponse response,
			Map<String, String> capturedHeaders,
			ByteArrayOutputStream capturedBody
	) {}

	private MockTransport mockTransport(String responseJson) throws IOException {
		return mockTransport(responseJson, 200);
	}

	private MockTransport mockTransport(String responseJson, int statusCode) throws IOException {
		ClientHttpRequestFactory factory = mock(ClientHttpRequestFactory.class);
		ClientHttpRequest request = mock(ClientHttpRequest.class);
		ClientHttpResponse response = mock(ClientHttpResponse.class);

		Map<String, String> headers = new LinkedHashMap<>();
		ByteArrayOutputStream body = new ByteArrayOutputStream();

		given(factory.createRequest(any(URI.class), any(HttpMethod.class))).willReturn(request);
		given(request.getHeaders()).willReturn(headers);
		given(request.getBody()).willReturn(body);
		given(request.execute()).willReturn(response);
		given(response.getStatusCode()).willReturn(statusCode);
		given(response.getBody()).willReturn(new ByteArrayInputStream(responseJson.getBytes(StandardCharsets.UTF_8)));

		return new MockTransport(factory, request, response, headers, body);
	}

	private ZaloBotClient buildClient(String token, ClientHttpRequestFactory factory) {
		return ZaloBotClient.builder()
				.botToken(token)
				.requestFactory(factory)
				.build();
	}

	// ── Builder tests ──────────────────────────────────────────────────

	@Test
	void builder_withBotToken_createsClient() {
		ZaloBotClient client = ZaloBotClient.botToken("test-token");
		assertThat(client).isNotNull();
	}

	@Test
	void builder_withNullBotToken_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> ZaloBotClient.builder().build())
				.withMessage("botToken must not be null");
	}

	@Test
	void builder_withCustomRequestFactory_usesIt() throws Exception {
		String json = """
				{"ok":true,"result":{"id":"1","account_name":"Bot","account_type":"official","can_join_groups":true},"error_code":0}""";
		MockTransport transport = mockTransport(json);

		ZaloBotClient client = buildClient("token", transport.factory());
		client.getMe().retrieve().call(GetMeResult.class);

		verify(transport.factory()).createRequest(any(URI.class), any(HttpMethod.class));
	}

	@Test
	void builder_withCustomUrl_usesIt() throws Exception {
		String json = """
				{"ok":true,"result":{"id":"1","account_name":"Bot","account_type":"official","can_join_groups":true},"error_code":0}""";
		MockTransport transport = mockTransport(json);

		ZaloBotClient client = ZaloBotClient.builder()
				.botToken("token")
				.zaloBotUrl(new ZaloBotUrl("http", "localhost", 9090))
				.requestFactory(transport.factory())
				.build();

		client.getMe().retrieve().call(GetMeResult.class);

		ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
		verify(transport.factory()).createRequest(uriCaptor.capture(), any(HttpMethod.class));
		URI uri = uriCaptor.getValue();
		assertThat(uri.getScheme()).isEqualTo("http");
		assertThat(uri.getHost()).isEqualTo("localhost");
		assertThat(uri.getPort()).isEqualTo(9090);
	}

	@Test
	void builder_defaultUrl_usesDefaultWhenNotSet() throws Exception {
		String json = """
				{"ok":true,"result":{"id":"1","account_name":"Bot","account_type":"official","can_join_groups":true},"error_code":0}""";
		MockTransport transport = mockTransport(json);

		ZaloBotClient client = buildClient("token", transport.factory());
		client.getMe().retrieve().call(GetMeResult.class);

		ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
		verify(transport.factory()).createRequest(uriCaptor.capture(), any(HttpMethod.class));
		URI uri = uriCaptor.getValue();
		assertThat(uri.getHost()).isEqualTo("bot-api.zaloplatforms.com");
		assertThat(uri.getPort()).isEqualTo(443);
	}

	@Test
	void builder_autoDetectsJdkClient() {
		// JDK HttpClient is always present on JDK 11+
		ZaloBotClient client = ZaloBotClient.builder()
				.botToken("token")
				.build();
		assertThat(client).isNotNull();
	}

	// ── URI path tests ─────────────────────────────────────────────────

	@Test
	void getMe_buildsCorrectUriPath() throws Exception {
		assertUriPath("getMe", client -> client.getMe().retrieve().call(GetMeResult.class));
	}

	@Test
	void getUpdates_buildsCorrectUriPath() throws Exception {
		assertUriPath("getUpdates", client -> client.getUpdates().retrieve()
				.call(dev.linhvu.zalobot.core.model.GetUpdatesResult.class));
	}

	@Test
	void sendMessage_buildsCorrectUriPath() throws Exception {
		assertUriPath("sendMessage", client -> client.sendMessage().retrieve().call(SendMessageResult.class));
	}

	@Test
	void sendPhoto_buildsCorrectUriPath() throws Exception {
		assertUriPath("sendPhoto", client -> client.sendPhoto().retrieve().call(SendMessageResult.class));
	}

	@Test
	void sendSticker_buildsCorrectUriPath() throws Exception {
		assertUriPath("sendSticker", client -> client.sendSticker().retrieve().call(SendMessageResult.class));
	}

	@Test
	void sendChatAction_buildsCorrectUriPath() throws Exception {
		assertUriPath("sendChatAction", client -> client.sendChatAction().retrieve().call(SendMessageResult.class));
	}

	private void assertUriPath(String expectedMethodPath, java.util.function.Consumer<ZaloBotClient> action) throws Exception {
		String json = """
				{"ok":true,"result":null,"error_code":0}""";
		MockTransport transport = mockTransport(json);
		ZaloBotClient client = buildClient("my-token", transport.factory());

		action.accept(client);

		ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
		verify(transport.factory()).createRequest(uriCaptor.capture(), any(HttpMethod.class));
		assertThat(uriCaptor.getValue().getPath()).isEqualTo("/botmy-token/" + expectedMethodPath);
	}

	// ── Fluent API tests ───────────────────────────────────────────────

	@Test
	void fluentApi_body_serializesRequestBody() throws Exception {
		String json = """
				{"ok":true,"result":{"message_id":"msg1","date":123},"error_code":0}""";
		MockTransport transport = mockTransport(json);
		ZaloBotClient client = buildClient("token", transport.factory());

		SendMessage msg = new SendMessage("chat1", "Hello");
		client.sendMessage().body(msg).retrieve().call(SendMessageResult.class);

		String bodyJson = transport.capturedBody().toString(StandardCharsets.UTF_8);
		assertThat(bodyJson).contains("\"chat_id\"");
		assertThat(bodyJson).contains("\"text\"");
		assertThat(bodyJson).contains("Hello");
	}

	@Test
	void fluentApi_header_setsCustomHeader() throws Exception {
		String json = """
				{"ok":true,"result":null,"error_code":0}""";
		MockTransport transport = mockTransport(json);
		ZaloBotClient client = buildClient("token", transport.factory());

		client.getMe()
				.header("X-Custom", "value")
				.retrieve()
				.call(GetMeResult.class);

		assertThat(transport.capturedHeaders()).containsEntry("X-Custom", "value");
	}

	@Test
	void fluentApi_defaultContentType_isApplicationJson() throws Exception {
		String json = """
				{"ok":true,"result":null,"error_code":0}""";
		MockTransport transport = mockTransport(json);
		ZaloBotClient client = buildClient("token", transport.factory());

		client.getMe().retrieve().call(GetMeResult.class);

		assertThat(transport.capturedHeaders()).containsEntry("Content-Type", "application/json");
	}

	// ── Response deserialization tests ──────────────────────────────────

	@Test
	void retrieve_call_deserializesSuccessResponse() throws Exception {
		String json = """
				{"ok":true,"result":{"id":"123","account_name":"TestBot","account_type":"official","can_join_groups":true},"error_code":0}""";
		MockTransport transport = mockTransport(json);
		ZaloBotClient client = buildClient("token", transport.factory());

		ZaloApiResponse<GetMeResult> response = client.getMe().retrieve().call(GetMeResult.class);
		assertThat(response.ok()).isTrue();
		assertThat(response.errorCode()).isZero();
		assertThat(response.result().id()).isEqualTo("123");
		assertThat(response.result().accountName()).isEqualTo("TestBot");
	}

	@Test
	void retrieve_call_throwsAuthenticationExceptionOnErrorResponse() throws Exception {
		String json = """
				{"ok":false,"result":null,"error_code":401}""";
		MockTransport transport = mockTransport(json, 401);
		ZaloBotClient client = buildClient("token", transport.factory());

		assertThatThrownBy(() -> client.getMe().retrieve().call(GetMeResult.class))
				.isInstanceOf(ZaloBotAuthenticationException.class)
				.satisfies(ex -> {
					ZaloBotAuthenticationException authEx = (ZaloBotAuthenticationException) ex;
					assertThat(authEx.getRawErrorCode()).isEqualTo(401);
					assertThat(authEx.getHttpStatus()).isEqualTo(401);
					assertThat(authEx.getErrorCode()).isEqualTo(ZaloErrorCode.UNAUTHORIZED);
				});
	}

	@Test
	void exchange_ioExceptionOnExecute_throwsZaloBotClientException() throws Exception {
		ClientHttpRequestFactory factory = mock(ClientHttpRequestFactory.class);
		ClientHttpRequest request = mock(ClientHttpRequest.class);
		Map<String, String> headers = new LinkedHashMap<>();

		given(factory.createRequest(any(URI.class), any(HttpMethod.class))).willReturn(request);
		given(request.getHeaders()).willReturn(headers);
		given(request.getBody()).willReturn(new ByteArrayOutputStream());
		given(request.execute()).willThrow(new IOException("connection refused"));

		ZaloBotClient client = buildClient("token", factory);

		assertThatThrownBy(() -> client.getMe().retrieve().call(GetMeResult.class))
				.isInstanceOf(ZaloBotClientException.class)
				.hasMessageContaining("connection refused");
	}

	// ── Full round-trip test ───────────────────────────────────────────

	@Test
	void sendMessage_fullRoundTrip() throws Exception {
		String json = """
				{"ok":true,"result":{"message_id":"msg1","date":1700000000},"error_code":0}""";
		MockTransport transport = mockTransport(json);
		ZaloBotClient client = buildClient("token", transport.factory());

		SendMessage msg = new SendMessage("chat1", "Hello World");
		ZaloApiResponse<SendMessageResult> response = client.sendMessage()
				.body(msg)
				.retrieve()
				.call(SendMessageResult.class);

		assertThat(response.ok()).isTrue();
		assertThat(response.result().messageId()).isEqualTo("msg1");
		assertThat(response.result().date()).isEqualTo(1700000000L);

		// Verify URI
		ArgumentCaptor<URI> uriCaptor = ArgumentCaptor.forClass(URI.class);
		verify(transport.factory()).createRequest(uriCaptor.capture(), any(HttpMethod.class));
		assertThat(uriCaptor.getValue().getPath()).isEqualTo("/bottoken/sendMessage");

		// Verify body was serialized
		String bodyJson = transport.capturedBody().toString(StandardCharsets.UTF_8);
		assertThat(bodyJson).contains("\"chat_id\":\"chat1\"");
		assertThat(bodyJson).contains("\"text\":\"Hello World\"");
	}

	// ── mutate() test ──────────────────────────────────────────────────

	@Test
	void mutate_returnsNewBuilder() throws Exception {
		String json = """
				{"ok":true,"result":null,"error_code":0}""";
		MockTransport transport = mockTransport(json);

		ZaloBotClient client = buildClient("token", transport.factory());
		// DefaultZaloBotClient is package-private, accessible from this package
		DefaultZaloBotClient defaultClient = (DefaultZaloBotClient) client;
		ZaloBotClient.Builder builder = defaultClient.mutate();
		assertThat(builder).isNotNull();

		ZaloBotClient newClient = builder.build();
		assertThat(newClient).isNotNull();
		assertThat(newClient).isNotSameAs(client);
	}
}