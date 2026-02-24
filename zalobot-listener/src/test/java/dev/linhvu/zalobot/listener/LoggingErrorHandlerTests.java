package dev.linhvu.zalobot.listener;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;

class LoggingErrorHandlerTests {

	private Logger logger;
	private ListAppender<ILoggingEvent> appender;

	@BeforeEach
	void setUp() {
		logger = (Logger) LoggerFactory.getLogger(LoggingErrorHandler.class);
		appender = new ListAppender<>();
		appender.start();
		logger.addAppender(appender);
	}

	@AfterEach
	void tearDown() {
		logger.detachAppender(appender);
		appender.stop();
	}

	@Test
	void handleError_doesNotThrow() {
		LoggingErrorHandler handler = new LoggingErrorHandler();
		UpdateListenerContainer container = mock(UpdateListenerContainer.class);
		assertThatCode(() -> handler.handleError(new RuntimeException("test error"), container))
				.doesNotThrowAnyException();
	}

	@Test
	void handleError_logsErrorWithMessage() {
		LoggingErrorHandler handler = new LoggingErrorHandler();
		UpdateListenerContainer container = mock(UpdateListenerContainer.class);

		handler.handleError(new RuntimeException("test error message"), container);

		assertThat(appender.list).hasSize(1);
		ILoggingEvent event = appender.list.get(0);
		assertThat(event.getLevel()).isEqualTo(Level.ERROR);
		assertThat(event.getFormattedMessage()).contains("Error in listener container")
				.contains("test error message");
		assertThat(event.getThrowableProxy()).isNotNull();
		assertThat(event.getThrowableProxy().getMessage()).isEqualTo("test error message");
	}
}
