package dev.linhvu.zalobot.client.exception;

/**
 * Exception thrown when the Zalo Bot API rejects a request due to
 * authentication or authorization failure.
 *
 * <p>This is a specialized subclass of {@link ZaloBotApiException} for errors
 * such as invalid access tokens, expired tokens, or unauthorized access.
 *
 * @see ZaloErrorCode#isAuthenticationError()
 */
public class ZaloBotAuthenticationException extends ZaloBotApiException {

	/**
	 * Creates a new authentication exception with the given error details.
	 *
	 * @param httpStatus the HTTP status code of the response
	 * @param errorCode the raw error code returned by the Zalo API
	 * @param description a human-readable description of the error
	 */
	public ZaloBotAuthenticationException(int httpStatus, int errorCode, String description) {
		super(httpStatus, errorCode, description);
	}

}
