package dev.linhvu.zalobot.client.exception;

/**
 * Base unchecked exception for all Zalo Bot SDK errors.
 *
 * <p>All exceptions thrown by the Zalo Bot client extend this class, making it
 * convenient to catch all SDK-related errors in a single handler.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloBotApiException
 * @see ZaloBotClientException
 * @see ZaloBotSerializationException
 */
public class ZaloBotException extends RuntimeException {

	/** Creates a new exception with no message or cause. */
	public ZaloBotException() {
	}

	/**
	 * Creates a new exception with the given message.
	 * @param message the detail message
	 */
	public ZaloBotException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception with the given message and cause.
	 * @param message the detail message
	 * @param cause the cause
	 */
	public ZaloBotException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new exception with the given cause.
	 * @param cause the cause
	 */
	public ZaloBotException(Throwable cause) {
		super(cause);
	}
}
