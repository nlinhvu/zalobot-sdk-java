package dev.linhvu.zalobot.client.exception;

import java.util.HashMap;
import java.util.Map;

/**
 * Enumeration of known Zalo Bot API error codes.
 *
 * <p>Each constant maps a numeric error code from the Zalo API to a descriptive
 * name and message. Use {@link #fromCode(int)} to look up an error code, and
 * {@link #isAuthenticationError()} to check if the error relates to authentication.
 */
public enum ZaloErrorCode {

	/** The request was successful. */
	SUCCESS(0, "Success"),

	// Authentication errors
	/** The access token is invalid or has expired. */
	INVALID_ACCESS_TOKEN(-216, "Invalid or expired access token"),
	/** The access token has expired and needs to be refreshed. */
	ACCESS_TOKEN_EXPIRED(-201, "Access token has expired"),
	/** The Official Account (OA) access token is invalid. */
	INVALID_OA_TOKEN(-213, "Invalid OA access token"),
	/** The request is unauthorized due to invalid credentials. */
	UNAUTHORIZED(401, "Unauthorized - invalid credentials"),

	// Rate limiting
	/** The API rate limit has been exceeded. */
	RATE_LIMIT_EXCEEDED(-117, "API rate limit exceeded"),

	// Message errors
	/** The message exceeds the maximum allowed length. */
	MESSAGE_TOO_LONG(-114, "Message exceeds maximum length"),
	/** The message type is not valid. */
	INVALID_MESSAGE_TYPE(-115, "Invalid message type"),

	// General errors
	/** An internal server error occurred on the Zalo side. */
	INTERNAL_SERVER_ERROR(-1, "Zalo internal server error"),
	/** One or more request parameters are invalid. */
	INVALID_PARAMETER(-100, "Invalid parameter"),
	/** The bot does not have permission to perform this action. */
	PERMISSION_DENIED(-103, "Permission denied for this action"),

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
		return this == INVALID_ACCESS_TOKEN
				|| this == ACCESS_TOKEN_EXPIRED
				|| this == INVALID_OA_TOKEN
				|| this == UNAUTHORIZED;
	}

	/**
	 * Returns {@code true} if this error code indicates success.
	 *
	 * @return {@code true} if the code is {@link #SUCCESS}
	 */
	public boolean isSuccess() {
		return this == SUCCESS;
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
