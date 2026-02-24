package dev.linhvu.zalobot.listener;

/**
 * Strategy interface for handling exceptions thrown during update polling or processing.
 *
 * @see LoggingErrorHandler
 */
@FunctionalInterface
public interface ErrorHandler {

	/**
	 * Handles an exception that occurred during listener processing.
	 *
	 * @param exception the exception that was thrown
	 * @param container the listener container where the error occurred
	 */
	void handleError(Exception exception, UpdateListenerContainer container);
}