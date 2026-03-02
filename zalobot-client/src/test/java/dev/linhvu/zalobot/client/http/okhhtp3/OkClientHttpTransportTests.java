package dev.linhvu.zalobot.client.http.okhhtp3;

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

class OkClientHttpTransportTests {

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
	void factory_createsRequests() {
		OkClientHttpRequestFactory factory = new OkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/test");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		assertThat(request).isNotNull();
		assertThat(request.getURI()).isEqualTo(uri);
		assertThat(request.getMethod()).isEqualTo(HttpMethod.POST);
	}

	@Test
	void execute_postWithBody_returnsResponse() throws Exception {
		this.server.createContext("/test", exchange -> {
			byte[] response = """
					{"ok":true}""".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		OkClientHttpRequestFactory factory = new OkClientHttpRequestFactory();
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
	void execute_postWithEmptyJsonBody_returnsResponse() throws Exception {
		this.server.createContext("/empty", exchange -> {
			byte[] response = "{}".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		OkClientHttpRequestFactory factory = new OkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/empty");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		// OkHttp requires a body for POST, so provide minimal body
		request.getHeaders().put("Content-Type", "application/json");
		request.getBody().write("{}".getBytes(StandardCharsets.UTF_8));

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
		}
	}

	@Test
	void execute_headersAreSent() throws Exception {
		java.util.concurrent.atomic.AtomicReference<String> receivedHeader =
				new java.util.concurrent.atomic.AtomicReference<>();
		this.server.createContext("/headers", exchange -> {
			receivedHeader.set(exchange.getRequestHeaders().getFirst("X-Custom"));
			byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		OkClientHttpRequestFactory factory = new OkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/headers");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		request.getHeaders().put("Content-Type", "application/json");
		request.getHeaders().put("X-Custom", "test-value");
		request.getBody().write("{}".getBytes(StandardCharsets.UTF_8));

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
		}
		assertThat(receivedHeader.get()).isEqualTo("test-value");
	}

	@Test
	void execute_returnsCorrectStatusCode() throws Exception {
		this.server.createContext("/error", exchange -> {
			byte[] response = "error".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(500, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		OkClientHttpRequestFactory factory = new OkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/error");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		request.getHeaders().put("Content-Type", "application/json");
		request.getBody().write("{}".getBytes(StandardCharsets.UTF_8));

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(500);
		}
	}

	@Test
	void execute_postWithEmptyHeaders_succeeds() throws Exception {
		this.server.createContext("/noheaders", exchange -> {
			byte[] response = "ok".getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(200, response.length);
			exchange.getResponseBody().write(response);
			exchange.close();
		});

		OkClientHttpRequestFactory factory = new OkClientHttpRequestFactory();
		URI uri = URI.create("http://localhost:" + this.port + "/noheaders");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		// Don't set any headers — covers the empty headers branch
		request.getBody().write("{}".getBytes(StandardCharsets.UTF_8));

		try (ClientHttpResponse response = request.execute()) {
			assertThat(response.getStatusCode()).isEqualTo(200);
		}
	}

	@Test
	void execute_serverFailure_throwsZaloBotClientException() throws Exception {
		// Use OkHttpClient that connects to a non-existent server to trigger onFailure callback
		OkClientHttpRequestFactory factory = new OkClientHttpRequestFactory();
		// Use a port that's not listening
		URI uri = URI.create("http://localhost:1/nonexistent");
		ClientHttpRequest request = factory.createRequest(uri, HttpMethod.POST);
		request.getHeaders().put("Content-Type", "application/json");
		request.getBody().write("{}".getBytes(StandardCharsets.UTF_8));

		assertThatThrownBy(request::execute)
				.isInstanceOf(dev.linhvu.zalobot.client.exception.ZaloBotClientException.class)
				.hasMessageContaining("HTTP request failed");
	}

	@Test
	void request_getBody_isNotNull() {
		OkClientHttpRequestFactory factory = new OkClientHttpRequestFactory();
		ClientHttpRequest request = factory.createRequest(
				URI.create("http://localhost/test"), HttpMethod.POST);
		assertThat(request.getBody()).isNotNull();
		assertThat(request.getHeaders()).isNotNull();
	}
}
