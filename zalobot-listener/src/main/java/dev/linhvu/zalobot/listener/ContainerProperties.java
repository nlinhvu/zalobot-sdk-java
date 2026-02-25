package dev.linhvu.zalobot.listener;

import java.time.Duration;

/**
 * Configuration properties for an {@link UpdateListenerContainer}.
 *
 * <p>Controls polling behavior, error handling, queue capacity, and
 * processing concurrency.
 *
 * <table>
 *   <caption>Default values</caption>
 *   <tr><th>Property</th><th>Default</th></tr>
 *   <tr><td>pollTimeout</td><td>30 seconds</td></tr>
 *   <tr><td>shutdownTimeout</td><td>10 seconds</td></tr>
 *   <tr><td>backOffInterval</td><td>1 second</td></tr>
 *   <tr><td>maxBackOffInterval</td><td>30 seconds</td></tr>
 *   <tr><td>queueCapacity</td><td>64</td></tr>
 *   <tr><td>processingConcurrency</td><td>1</td></tr>
 * </table>
 *
 * @author Linh Vu
 * @since 0.0.1
 */
public class ContainerProperties {

	/** The long-polling timeout for the getUpdates API call. Default: 30 seconds. */
	private Duration pollTimeout = Duration.ofSeconds(30);
	/** The maximum time to wait for the container to shut down gracefully. Default: 10 seconds. */
	private Duration shutdownTimeout = Duration.ofSeconds(10);
	/** The initial backoff interval after an error. Default: 1 second. */
	private Duration backOffInterval = Duration.ofSeconds(1);
	/** The maximum backoff interval. Default: 30 seconds. */
	private Duration maxBackOffInterval = Duration.ofSeconds(30);
	private int queueCapacity = 64;
	private int processingConcurrency = 1;
	private UpdateListener updateListener;
	private ErrorHandler errorHandler;

	/**
	 * Returns the long-polling timeout for the {@code getUpdates} API call.
	 * @return the poll timeout
	 */
	public Duration getPollTimeout() {
		return pollTimeout;
	}

	/**
	 * Sets the long-polling timeout for the {@code getUpdates} API call.
	 * @param pollTimeout the poll timeout
	 */
	public void setPollTimeout(Duration pollTimeout) {
		this.pollTimeout = pollTimeout;
	}

	/**
	 * Returns the maximum time to wait for the container to shut down gracefully.
	 * @return the shutdown timeout
	 */
	public Duration getShutdownTimeout() {
		return shutdownTimeout;
	}

	/**
	 * Sets the maximum time to wait for the container to shut down gracefully.
	 * @param shutdownTimeout the shutdown timeout
	 */
	public void setShutdownTimeout(Duration shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}

	/**
	 * Returns the initial backoff interval after a polling error.
	 * @return the backoff interval
	 */
	public Duration getBackOffInterval() {
		return backOffInterval;
	}

	/**
	 * Sets the initial backoff interval after a polling error.
	 * @param backOffInterval the backoff interval
	 */
	public void setBackOffInterval(Duration backOffInterval) {
		this.backOffInterval = backOffInterval;
	}

	/**
	 * Returns the maximum backoff interval.
	 * @return the maximum backoff interval
	 */
	public Duration getMaxBackOffInterval() {
		return maxBackOffInterval;
	}

	/**
	 * Sets the maximum backoff interval.
	 * @param maxBackOffInterval the maximum backoff interval
	 */
	public void setMaxBackOffInterval(Duration maxBackOffInterval) {
		this.maxBackOffInterval = maxBackOffInterval;
	}

	/**
	 * Returns the capacity of the bounded queue between the polling thread and
	 * the processing threads.
	 * @return the queue capacity
	 */
	public int getQueueCapacity() {
		return queueCapacity;
	}

	/**
	 * Sets the capacity of the bounded queue between the polling thread and
	 * the processing threads.
	 * @param queueCapacity the queue capacity (must be &gt; 0)
	 * @throws IllegalArgumentException if {@code queueCapacity} is not positive
	 */
	public void setQueueCapacity(int queueCapacity) {
		if (queueCapacity <= 0) {
			throw new IllegalArgumentException("'queueCapacity' must be greater than 0");
		}
		this.queueCapacity = queueCapacity;
	}

	/**
	 * Returns the number of concurrent processing threads.
	 * @return the processing concurrency
	 */
	public int getProcessingConcurrency() {
		return processingConcurrency;
	}

	/**
	 * Sets the number of concurrent processing threads.
	 * @param processingConcurrency the processing concurrency (must be &gt; 0)
	 * @throws IllegalArgumentException if {@code processingConcurrency} is not positive
	 */
	public void setProcessingConcurrency(int processingConcurrency) {
		if (processingConcurrency <= 0) {
			throw new IllegalArgumentException(
					"'processingConcurrency' must be greater than 0");
		}
		this.processingConcurrency = processingConcurrency;
	}

	/**
	 * Returns the update listener registered with this container.
	 * @return the update listener, or {@code null} if not yet set
	 */
	public UpdateListener getUpdateListener() {
		return updateListener;
	}

	/**
	 * Sets the update listener.
	 * @param updateListener the update listener
	 */
	public void setUpdateListener(UpdateListener updateListener) {
		this.updateListener = updateListener;
	}

	/**
	 * Returns the error handler for this container.
	 * @return the error handler, or {@code null} to use the default
	 */
	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	/**
	 * Sets the error handler. If {@code null}, a {@link LoggingErrorHandler}
	 * will be used as the default.
	 * @param errorHandler the error handler, or {@code null}
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
}
