package dev.linhvu.zalobot.client.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of known Zalo Bot API error codes.
 *
 * <p>Each constant maps a numeric error code from the Zalo API to a descriptive
 * name and message. Use {@link #fromCode(int)} to look up an error code, and
 * {@link #isAuthenticationError()} to check if the error relates to authentication.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloBotApiException
 * @see ZaloBotRequestTimeoutException
 */
public enum ZaloErrorCode {

	/** The request was malformed or contained invalid parameters. */
	BAD_REQUEST(400, "Bad request"),
	/** The request is unauthorized due to invalid credentials. */
	UNAUTHORIZED(401, "Unauthorized"),
	/** The server encountered an unexpected condition. */
	INTERNAL_SERVER_ERROR(403, "Internal server error"),
	/** The requested resource could not be found. */
	NOT_FOUND(404, "Not found"),
	/** The server did not respond within the expected time limit. */
	REQUEST_TIMEOUT(408, "Request timeout"),
	/** The request has been rate-limited due to exceeding the allowed quota. */
	QUOTA_EXCEEDED(429, "Quota exceeded"),
	/** Fallback for unrecognized error codes. */
	UNKNOWN(Integer.MIN_VALUE, "Unknown error code");

	private static final Map<Integer, ZaloErrorCode> CODE_MAP = new HashMap<>();

	static {
		for (ZaloErrorCode value : values()) {
			CODE_MAP.put(value.code, value);
		}
	}

	private final int code;
	private final String description;

	ZaloErrorCode(int code, String description) {
		this.code = code;
		this.description = description;
	}

	/**
	 * Returns the numeric error code.
	 *
	 * @return the error code
	 */
	public int getCode() {
		return this.code;
	}

	/**
	 * Returns a human-readable description of this error code.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return this.description;
	}

	/**
	 * Returns {@code true} if this error code indicates an authentication
	 * or authorization failure.
	 *
	 * @return {@code true} for authentication-related errors
	 */
	public boolean isAuthenticationError() {
		return this == UNAUTHORIZED;
	}

	/**
	 * Returns {@code true} if this error code indicates a request timeout.
	 *
	 * @return {@code true} for timeout-related errors
	 */
	public boolean isRequestTimeout() {
		return this == REQUEST_TIMEOUT;
	}

	/**
	 * Looks up a {@code ZaloErrorCode} by its numeric code.
	 *
	 * @param code the numeric error code
	 * @return the matching enum constant, or {@link #UNKNOWN} if not found
	 */
	public static ZaloErrorCode fromCode(int code) {
		return CODE_MAP.getOrDefault(code, UNKNOWN);
	}

}
