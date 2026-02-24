package dev.linhvu.zalobot.client.exception;

/**
 * Base unchecked exception for all Zalo Bot SDK errors.
 *
 * <p>All exceptions thrown by the Zalo Bot client extend this class, making it
 * convenient to catch all SDK-related errors in a single handler.
 *
 * @see ZaloBotApiException
 * @see ZaloBotClientException
 * @see ZaloBotSerializationException
 */
public class ZaloBotException extends RuntimeException {

	public ZaloBotException() {
	}

	public ZaloBotException(String message) {
		super(message);
	}

	public ZaloBotException(String message, Throwable cause) {
		super(message, cause);
	}

	public ZaloBotException(Throwable cause) {
		super(cause);
	}
}
