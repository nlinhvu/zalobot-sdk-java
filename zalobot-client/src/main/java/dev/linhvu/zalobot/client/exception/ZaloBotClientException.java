package dev.linhvu.zalobot.client.exception;

/**
 * Exception thrown when an HTTP transport-level error occurs while
 * communicating with the Zalo Bot API.
 *
 * <p>This covers network failures, connection timeouts, malformed URIs,
 * and other I/O-related errors that are not API-level errors.
 */
public class ZaloBotClientException extends ZaloBotException {

	/**
	 * Creates a new client exception with the given message and cause.
	 *
	 * @param message a description of the error
	 * @param cause the underlying cause
	 */
	public ZaloBotClientException(String message, Throwable cause) {
		super(message, cause);
	}

}
