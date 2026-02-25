package dev.linhvu.zalobot.client.exception;

/**
 * Exception thrown when the Zalo Bot API returns an error response.
 *
 * <p>Contains detailed error information including the HTTP status code,
 * the Zalo-specific error code, and a human-readable description.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloErrorCode
 * @see ZaloBotAuthenticationException
 * @see ZaloBotRequestTimeoutException
 */
public class ZaloBotApiException extends ZaloBotException {

	private final int httpStatus;
	private final ZaloErrorCode errorCode;
	private final int rawErrorCode;
	private final String description;

	/**
	 * Creates a new API exception with the given error details.
	 *
	 * @param httpStatus the HTTP status code of the response
	 * @param rawErrorCode the raw error code returned by the Zalo API
	 * @param description a human-readable description of the error
	 */
	public ZaloBotApiException(int httpStatus, int rawErrorCode, String description) {
		super("Zalo API error: [%d] %s (HTTP %d)".formatted(rawErrorCode, description, httpStatus));
		this.httpStatus = httpStatus;
		this.errorCode = ZaloErrorCode.fromCode(rawErrorCode);
		this.rawErrorCode = rawErrorCode;
		this.description = description;
	}

	/**
	 * Returns the HTTP status code of the error response.
	 *
	 * @return the HTTP status code
	 */
	public int getHttpStatus() {
		return this.httpStatus;
	}

	/**
	 * Returns the mapped {@link ZaloErrorCode} enum constant.
	 *
	 * @return the error code enum, or {@link ZaloErrorCode#UNKNOWN} if unrecognized
	 */
	public ZaloErrorCode getErrorCode() {
		return this.errorCode;
	}

	/**
	 * Returns the raw integer error code as returned by the Zalo API.
	 *
	 * @return the raw error code
	 */
	public int getRawErrorCode() {
		return this.rawErrorCode;
	}

	/**
	 * Returns a human-readable description of the error.
	 *
	 * @return the error description
	 */
	public String getDescription() {
		return this.description;
	}

}
