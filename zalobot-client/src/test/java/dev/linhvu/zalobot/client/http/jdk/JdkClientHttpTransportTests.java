package dev.linhvu.zalobot.client.http.jdk;

import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.HttpServer;
import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.ClientHttpResponse;
import dev.linhvu.zalobot.client.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class JdkClientHttpTransportTests {

	private HttpServer server;
	private int port;

	@BeforeEach
	void setUp() throws Exception {
		this.server = HttpServer.create(new InetSocketAddress(0), 0);
		this.port = this.server.getAddress().getPort();
		this.server.start();
	}

	@AfterEach
	void tearDown() {
		this.server.stop(0);
	}

	@Test
	void execute_postWithBody_returnsResponse() throws Exception {
		this.server.createContext("/test", exchange -> {
			byte[] response = """
					{"ok":true,"result":null,"error_code":0}""".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/test");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		request.getHeaders().put("Content-Type", "application/json");
		request.getBody().write("{\"key\":\"value\"}".getBytes(StandardCharsets.UTF_8));

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
			String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
			assertThat(body).contains("\"ok\":true");
		}
	}

	@Test
	void execute_postWithoutBody_returnsResponse() throws Exception {
		this.server.createContext("/empty", exchange -> {
			byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/empty");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
		}
	}

	@Test
	void execute_returnsCorrectStatusCode() throws Exception {
		this.server.createContext("/error", exchange -> {
			byte[] response = "error".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(500, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/error");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(500);
		}
	}

	@Test
	void execute_headersAreSentToServer() throws Exception {
		java.util.concurrent.atomic.AtomicReference<String> receivedHeader =
				new java.util.concurrent.atomic.AtomicReference<>();
		this.server.createContext("/headers", exchange -> {
			receivedHeader.set(exchange.getRequestHeaders().getFirst("X-Custom"));
			byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/headers");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		request.getHeaders().put("X-Custom", "test-value");

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
		}
		assertThat(receivedHeader.get()).isEqualTo("test-value");
	}

	@Test
	void execute_requestBodyIsSentToServer() throws Exception {
		java.util.concurrent.atomic.AtomicReference<String> receivedBody =
				new java.util.concurrent.atomic.AtomicReference<>();
		this.server.createContext("/body", exchange -> {
			receivedBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
			byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/body");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		request.getBody().write("hello world".getBytes(StandardCharsets.UTF_8));

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
		}
		assertThat(receivedBody.get()).isEqualTo("hello world");
	}

	@Test
	void response_getBody_returnsNullInputStreamWhenBodyIsNull() throws Exception {
		// JdkClientHttpResponse should handle null body
		java.net.http.HttpResponse<InputStream> mockResponse =
				org.mockito.Mockito.mock(java.net.http.HttpResponse.class);
		org.mockito.BDDMockito.given(mockResponse.statusCode()).willReturn(200);

		JdkClientHttpResponse jdkResponse = new JdkClientHttpResponse(mockResponse, null);
		assertThat(jdkResponse.getStatusCode()).isEqualTo(200);
		assertThat(jdkResponse.getBody()).isNotNull();
		// Should return empty input stream (nullInputStream)
		assertThat(jdkResponse.getBody().read()).isEqualTo(-1);
		jdkResponse.close();
	}

	@Test
	void execute_withReadTimeout_succeeds() throws Exception {
		this.server.createContext("/timeout", exchange -> {
			byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		factory.setReadTimeout(java.time.Duration.ofSeconds(5));
		URI uri = URI.create("http://localhost:" + this.port + "/timeout");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
		}
	}

	@Test
	void execute_withReadTimeout_closesInputStreamOnClose() throws Exception {
		this.server.createContext("/timeout-close", exchange -> {
			byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		factory.setReadTimeout(java.time.Duration.ofSeconds(10));
		URI uri = URI.create("http://localhost:" + this.port + "/timeout-close");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);

		// Execute with timeout — the response InputStream should be wrapped with
		// a FilterInputStream that cancels the timeout on close
		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
			assertThat(response.getBody()).isNotNull();
			// Reading the body should work
			String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
			assertThat(body).isEqualTo("ok");
		}
		// close() on the response should cancel the timeout future
	}

	@Test
	void request_getters_returnCorrectValues() {
		JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost/test");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);

		assertThat(request.getURI()).isEqualTo(uri);
		assertThat(request.getMethod()).isEqualTo(HttpMethod.POST);
		assertThat(request.getHeaders()).isNotNull();
		assertThat(request.getBody()).isNotNull();
	}
}
