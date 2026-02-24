package dev.linhvu.zalobot.listener;

import java.time.Duration;
import java.util.concurrent.Executor;

/**
 * Configuration properties for an {@link UpdateListenerContainer}.
 *
 * <p>Controls polling behavior, error handling, and execution threading.
 *
 * <table>
 *   <caption>Default values</caption>
 *   <tr><th>Property</th><th>Default</th></tr>
 *   <tr><td>pollTimeout</td><td>30 seconds</td></tr>
 *   <tr><td>pollInterval</td><td>0 seconds (no delay between polls)</td></tr>
 *   <tr><td>shutdownTimeout</td><td>10 seconds</td></tr>
 *   <tr><td>backOffInterval</td><td>1 second</td></tr>
 *   <tr><td>maxBackOffInterval</td><td>30 seconds</td></tr>
 * </table>
 */
public class ContainerProperties {

	/** The long-polling timeout for the getUpdates API call. Default: 30 seconds. */
	private Duration pollTimeout = Duration.ofSeconds(30);
	/** The delay between consecutive polls. Default: 0 seconds (immediate). */
	private Duration pollInterval = Duration.ofSeconds(0);
	/** The maximum time to wait for the container to shut down gracefully. Default: 10 seconds. */
	private Duration shutdownTimeout = Duration.ofSeconds(10);
	/** The initial backoff interval after an error. Default: 1 second. */
	private Duration backOffInterval = Duration.ofSeconds(1);
	/** The maximum backoff interval. Default: 30 seconds. */
	private Duration maxBackOffInterval = Duration.ofSeconds(30);
	private Executor listenerTaskExecutor;
	private UpdateListener updateListener;
	private ErrorHandler errorHandler;

	public Duration getPollTimeout() {
		return pollTimeout;
	}

	public void setPollTimeout(Duration pollTimeout) {
		this.pollTimeout = pollTimeout;
	}

	public Duration getPollInterval() {
		return pollInterval;
	}

	public void setPollInterval(Duration pollInterval) {
		this.pollInterval = pollInterval;
	}

	public Duration getShutdownTimeout() {
		return shutdownTimeout;
	}

	public void setShutdownTimeout(Duration shutdownTimeout) {
		this.shutdownTimeout = shutdownTimeout;
	}

	public Duration getBackOffInterval() {
		return backOffInterval;
	}

	public void setBackOffInterval(Duration backOffInterval) {
		this.backOffInterval = backOffInterval;
	}

	public Duration getMaxBackOffInterval() {
		return maxBackOffInterval;
	}

	public void setMaxBackOffInterval(Duration maxBackOffInterval) {
		this.maxBackOffInterval = maxBackOffInterval;
	}

	public Executor getListenerTaskExecutor() {
		return listenerTaskExecutor;
	}

	public void setListenerTaskExecutor(Executor listenerTaskExecutor) {
		this.listenerTaskExecutor = listenerTaskExecutor;
	}

	public UpdateListener getUpdateListener() {
		return updateListener;
	}

	public void setUpdateListener(UpdateListener updateListener) {
		this.updateListener = updateListener;
	}

	public ErrorHandler getErrorHandler() {
		return errorHandler;
	}

	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}
}
