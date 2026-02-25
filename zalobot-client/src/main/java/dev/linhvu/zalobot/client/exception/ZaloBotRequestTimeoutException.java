package dev.linhvu.zalobot.client.exception;

/**
 * Exception thrown when the Zalo Bot API returns a request timeout error.
 *
 * <p>This is a specialized subclass of {@link ZaloBotApiException} for errors
 * where the server did not respond within the expected time limit.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloErrorCode#isRequestTimeout()
 */
public class ZaloBotRequestTimeoutException extends ZaloBotApiException {

	/**
	 * Creates a new request timeout exception with the given error details.
	 *
	 * @param httpStatus the HTTP status code of the response
	 * @param errorCode the raw error code returned by the Zalo API
	 * @param description a human-readable description of the error
	 */
	public ZaloBotRequestTimeoutException(int httpStatus, int errorCode, String description) {
		super(httpStatus, errorCode, description);
	}
}
