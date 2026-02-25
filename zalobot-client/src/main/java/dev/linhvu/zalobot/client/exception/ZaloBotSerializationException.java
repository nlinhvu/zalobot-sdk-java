package dev.linhvu.zalobot.client.exception;

/**
 * Exception thrown when JSON serialization or deserialization of request/response
 * bodies fails.
 *
 * <p>This typically indicates a mismatch between the expected and actual JSON
 * structure, or a Jackson configuration issue.
 *
 * @author Linh Vu
 * @since 0.0.1
 */
public class ZaloBotSerializationException extends ZaloBotException {

	/**
	 * Creates a new serialization exception with the given message and cause.
	 *
	 * @param message a description of the error
	 * @param cause the underlying cause (usually a Jackson exception)
	 */
	public ZaloBotSerializationException(String message, Throwable cause) {
		super(message, cause);
	}

}
