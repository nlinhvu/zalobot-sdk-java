package dev.linhvu.zalobot.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default {@link ErrorHandler} implementation that logs errors at ERROR level
 * using SLF4J.
 */
public class LoggingErrorHandler implements ErrorHandler {

	private static final Logger logger = LoggerFactory.getLogger(LoggingErrorHandler.class);

	@Override
	public void handleError(Exception exception, UpdateListenerContainer container) {
		logger.error("Error in listener container: {}", exception.getMessage(), exception);
	}
}
