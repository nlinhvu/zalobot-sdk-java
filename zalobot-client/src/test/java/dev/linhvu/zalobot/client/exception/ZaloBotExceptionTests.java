package dev.linhvu.zalobot.client.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ZaloBotExceptionTests {

	// ── ZaloBotException (base) ───────────────────────────────────────

	@Test
	void zaloBotException_noArgs() {
		ZaloBotException ex = new ZaloBotException();
		assertThat(ex.getMessage()).isNull();
		assertThat(ex.getCause()).isNull();
	}

	@Test
	void zaloBotException_withMessage() {
		ZaloBotException ex = new ZaloBotException("test message");
		assertThat(ex.getMessage()).isEqualTo("test message");
	}

	@Test
	void zaloBotException_withMessageAndCause() {
		Throwable cause = new RuntimeException("root");
		ZaloBotException ex = new ZaloBotException("wrapped", cause);
		assertThat(ex.getMessage()).isEqualTo("wrapped");
		assertThat(ex.getCause()).isSameAs(cause);
	}

	@Test
	void zaloBotException_withCauseOnly() {
		Throwable cause = new RuntimeException("root");
		ZaloBotException ex = new ZaloBotException(cause);
		assertThat(ex.getCause()).isSameAs(cause);
	}

	@Test
	void zaloBotException_isRuntimeException() {
		assertThat(new ZaloBotException()).isInstanceOf(RuntimeException.class);
	}

	// ── ZaloBotApiException ────────────────────────────────────────────

	@Test
	void apiException_storesFieldsCorrectly() {
		ZaloBotApiException ex = new ZaloBotApiException(200, 400, "Bad request");
		assertThat(ex.getHttpStatus()).isEqualTo(200);
		assertThat(ex.getRawErrorCode()).isEqualTo(400);
		assertThat(ex.getDescription()).isEqualTo("Bad request");
		assertThat(ex.getErrorCode()).isEqualTo(ZaloErrorCode.BAD_REQUEST);
	}

	@Test
	void apiException_messageFormat() {
		ZaloBotApiException ex = new ZaloBotApiException(200, 404, "Not found");
		assertThat(ex.getMessage()).isEqualTo("Zalo API error: [404] Not found (HTTP 200)");
	}

	@Test
	void apiException_unknownErrorCode() {
		ZaloBotApiException ex = new ZaloBotApiException(500, 999, "Unknown");
		assertThat(ex.getErrorCode()).isEqualTo(ZaloErrorCode.UNKNOWN);
	}

	@Test
	void apiException_extendsZaloBotException() {
		assertThat(new ZaloBotApiException(200, 400, "test"))
				.isInstanceOf(ZaloBotException.class);
	}

	// ── ZaloBotAuthenticationException ─────────────────────────────────

	@Test
	void authException_storesFieldsCorrectly() {
		ZaloBotAuthenticationException ex = new ZaloBotAuthenticationException(401, 401, "Unauthorized");
		assertThat(ex.getHttpStatus()).isEqualTo(401);
		assertThat(ex.getRawErrorCode()).isEqualTo(401);
		assertThat(ex.getDescription()).isEqualTo("Unauthorized");
		assertThat(ex.getErrorCode()).isEqualTo(ZaloErrorCode.UNAUTHORIZED);
	}

	@Test
	void authException_extendsApiException() {
		assertThat(new ZaloBotAuthenticationException(401, 401, "Unauthorized"))
				.isInstanceOf(ZaloBotApiException.class);
	}

	// ── ZaloBotRequestTimeoutException ─────────────────────────────────

	@Test
	void timeoutException_storesFieldsCorrectly() {
		ZaloBotRequestTimeoutException ex = new ZaloBotRequestTimeoutException(200, 408, "Request timeout");
		assertThat(ex.getHttpStatus()).isEqualTo(200);
		assertThat(ex.getRawErrorCode()).isEqualTo(408);
		assertThat(ex.getErrorCode()).isEqualTo(ZaloErrorCode.REQUEST_TIMEOUT);
	}

	@Test
	void timeoutException_extendsApiException() {
		assertThat(new ZaloBotRequestTimeoutException(200, 408, "timeout"))
				.isInstanceOf(ZaloBotApiException.class);
	}

	// ── ZaloBotClientException ─────────────────────────────────────────

	@Test
	void clientException_storesMessageAndCause() {
		Throwable cause = new java.io.IOException("connection refused");
		ZaloBotClientException ex = new ZaloBotClientException("request failed", cause);
		assertThat(ex.getMessage()).isEqualTo("request failed");
		assertThat(ex.getCause()).isSameAs(cause);
	}

	@Test
	void clientException_extendsZaloBotException() {
		assertThat(new ZaloBotClientException("msg", new RuntimeException()))
				.isInstanceOf(ZaloBotException.class);
	}

	// ── ZaloBotSerializationException ──────────────────────────────────

	@Test
	void serializationException_storesMessageAndCause() {
		Throwable cause = new RuntimeException("jackson error");
		ZaloBotSerializationException ex = new ZaloBotSerializationException("serialization failed", cause);
		assertThat(ex.getMessage()).isEqualTo("serialization failed");
		assertThat(ex.getCause()).isSameAs(cause);
	}

	@Test
	void serializationException_extendsZaloBotException() {
		assertThat(new ZaloBotSerializationException("msg", new RuntimeException()))
				.isInstanceOf(ZaloBotException.class);
	}
}
