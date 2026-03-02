package dev.linhvu.zalobot.client.exception;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ZaloErrorCodeTests {

	@Test
	void fromCode_knownCodes_returnCorrectEnum() {
		assertThat(ZaloErrorCode.fromCode(400)).isEqualTo(ZaloErrorCode.BAD_REQUEST);
		assertThat(ZaloErrorCode.fromCode(401)).isEqualTo(ZaloErrorCode.UNAUTHORIZED);
		assertThat(ZaloErrorCode.fromCode(403)).isEqualTo(ZaloErrorCode.INTERNAL_SERVER_ERROR);
		assertThat(ZaloErrorCode.fromCode(404)).isEqualTo(ZaloErrorCode.NOT_FOUND);
		assertThat(ZaloErrorCode.fromCode(408)).isEqualTo(ZaloErrorCode.REQUEST_TIMEOUT);
		assertThat(ZaloErrorCode.fromCode(429)).isEqualTo(ZaloErrorCode.QUOTA_EXCEEDED);
	}

	@Test
	void fromCode_unknownCode_returnsUnknown() {
		assertThat(ZaloErrorCode.fromCode(999)).isEqualTo(ZaloErrorCode.UNKNOWN);
		assertThat(ZaloErrorCode.fromCode(0)).isEqualTo(ZaloErrorCode.UNKNOWN);
		assertThat(ZaloErrorCode.fromCode(-1)).isEqualTo(ZaloErrorCode.UNKNOWN);
	}

	@Test
	void getCode_returnsCorrectValues() {
		assertThat(ZaloErrorCode.BAD_REQUEST.getCode()).isEqualTo(400);
		assertThat(ZaloErrorCode.UNAUTHORIZED.getCode()).isEqualTo(401);
		assertThat(ZaloErrorCode.REQUEST_TIMEOUT.getCode()).isEqualTo(408);
		assertThat(ZaloErrorCode.UNKNOWN.getCode()).isEqualTo(Integer.MIN_VALUE);
	}

	@Test
	void getDescription_returnsNonEmptyString() {
		for (ZaloErrorCode code : ZaloErrorCode.values()) {
			assertThat(code.getDescription()).isNotBlank();
		}
	}

	@Test
	void isAuthenticationError_onlyTrueForUnauthorized() {
		assertThat(ZaloErrorCode.UNAUTHORIZED.isAuthenticationError()).isTrue();
		assertThat(ZaloErrorCode.BAD_REQUEST.isAuthenticationError()).isFalse();
		assertThat(ZaloErrorCode.REQUEST_TIMEOUT.isAuthenticationError()).isFalse();
		assertThat(ZaloErrorCode.UNKNOWN.isAuthenticationError()).isFalse();
	}

	@Test
	void isRequestTimeout_onlyTrueForRequestTimeout() {
		assertThat(ZaloErrorCode.REQUEST_TIMEOUT.isRequestTimeout()).isTrue();
		assertThat(ZaloErrorCode.UNAUTHORIZED.isRequestTimeout()).isFalse();
		assertThat(ZaloErrorCode.BAD_REQUEST.isRequestTimeout()).isFalse();
		assertThat(ZaloErrorCode.UNKNOWN.isRequestTimeout()).isFalse();
	}
}
