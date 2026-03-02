package dev.linhvu.zalobot.boot.autoconfigure;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for ZaloBot.
 *
 * @see dev.linhvu.zalobot.client.ZaloBotClient
 * @see dev.linhvu.zalobot.listener.ContainerProperties
 *
 * @author Linh Vu
 * @since 0.0.1
 */
@ConfigurationProperties("zalobot")
public class ZaloBotProperties {

	/**
	 * Bot token for authenticating with the Zalo API. Required.
	 */
	private String botToken;
	private final Client client = new Client();
	private final Listener listener = new Listener();

	/**
	 * Returns the bot token.
	 * @return the bot token
	 */
	public String getBotToken() {
		return this.botToken;
	}

	/**
	 * Sets the bot token.
	 * @param botToken the bot token
	 */
	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	/**
	 * Returns the client connection properties.
	 * @return the client properties
	 */
	public Client getClient() {
		return this.client;
	}

	/**
	 * Returns the listener container properties.
	 * @return the listener properties
	 */
	public Listener getListener() {
		return this.listener;
	}

	/**
	 * Client connection properties.
	 * Maps to {@link dev.linhvu.zalobot.client.ZaloBotUrl}.
	 */
	public static class Client {

		/**
		 * URL scheme for the Zalo Bot API.
		 */
		private String scheme = "https";

		/**
		 * Hostname of the Zalo Bot API.
		 */
		private String host = "bot-api.zaloplatforms.com";

		/**
		 * Port of the Zalo Bot API.
		 */
		private int port = 443;

		/**
		 * Returns the URL scheme.
		 * @return the scheme
		 */
		public String getScheme() {
			return this.scheme;
		}

		/**
		 * Sets the URL scheme.
		 * @param scheme the scheme
		 */
		public void setScheme(String scheme) {
			this.scheme = scheme;
		}

		/**
		 * Returns the API hostname.
		 * @return the host
		 */
		public String getHost() {
			return this.host;
		}

		/**
		 * Sets the API hostname.
		 * @param host the host
		 */
		public void setHost(String host) {
			this.host = host;
		}

		/**
		 * Returns the API port.
		 * @return the port
		 */
		public int getPort() {
			return this.port;
		}

		/**
		 * Sets the API port.
		 * @param port the port
		 */
		public void setPort(int port) {
			this.port = port;
		}
	}

	/**
	 * Listener container properties.
	 * Maps to {@link dev.linhvu.zalobot.listener.ContainerProperties}.
	 */
	public static class Listener {

		/**
		 * Whether the listener container is enabled.
		 * When false, no listener container is created even if an UpdateListener bean exists.
		 */
		private boolean enabled = true;

		/**
		 * Timeout for long-polling the Zalo API for updates.
		 * Maps to ContainerProperties.pollTimeout.
		 */
		private Duration pollTimeout = Duration.ofSeconds(30);

		/**
		 * Maximum time to wait for the listener container to shut down gracefully.
		 * Maps to ContainerProperties.shutdownTimeout.
		 */
		private Duration shutdownTimeout = Duration.ofSeconds(10);

		/**
		 * Initial back-off interval after a poll error.
		 * Maps to ContainerProperties.backOffInterval.
		 */
		private Duration backOffInterval = Duration.ofSeconds(1);

		/**
		 * Maximum back-off interval after repeated poll errors.
		 * Maps to ContainerProperties.maxBackOffInterval.
		 */
		private Duration maxBackOffInterval = Duration.ofSeconds(30);

		/**
		 * Capacity of the bounded queue between the polling thread and processing threads.
		 */
		private int queueCapacity = 64;

		/**
		 * Number of concurrent threads for processing updates.
		 */
		private int processingConcurrency = 1;

		/**
		 * Whether to enable observation for the listener container.
		 * When {@code true} and an {@link io.micrometer.observation.ObservationRegistry}
		 * bean is available, observations will be recorded for each processed update.
		 */
		private boolean observationEnabled = true;

		/**
		 * Returns whether the listener container is enabled.
		 * @return {@code true} if enabled
		 */
		public boolean isEnabled() {
			return this.enabled;
		}

		/**
		 * Sets whether the listener container is enabled.
		 * @param enabled {@code true} to enable
		 */
		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		/**
		 * Returns the poll timeout.
		 * @return the poll timeout
		 */
		public Duration getPollTimeout() {
			return this.pollTimeout;
		}

		/**
		 * Sets the poll timeout.
		 * @param pollTimeout the poll timeout
		 */
		public void setPollTimeout(Duration pollTimeout) {
			this.pollTimeout = pollTimeout;
		}

		/**
		 * Returns the shutdown timeout.
		 * @return the shutdown timeout
		 */
		public Duration getShutdownTimeout() {
			return this.shutdownTimeout;
		}

		/**
		 * Sets the shutdown timeout.
		 * @param shutdownTimeout the shutdown timeout
		 */
		public void setShutdownTimeout(Duration shutdownTimeout) {
			this.shutdownTimeout = shutdownTimeout;
		}

		/**
		 * Returns the backoff interval.
		 * @return the backoff interval
		 */
		public Duration getBackOffInterval() {
			return this.backOffInterval;
		}

		/**
		 * Sets the backoff interval.
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
			return this.maxBackOffInterval;
		}

		/**
		 * Sets the maximum backoff interval.
		 * @param maxBackOffInterval the maximum backoff interval
		 */
		public void setMaxBackOffInterval(Duration maxBackOffInterval) {
			this.maxBackOffInterval = maxBackOffInterval;
		}

		/**
		 * Returns the queue capacity.
		 * @return the queue capacity
		 */
		public int getQueueCapacity() {
			return queueCapacity;
		}

		/**
		 * Sets the queue capacity.
		 * @param queueCapacity the queue capacity
		 */
		public void setQueueCapacity(int queueCapacity) {
			this.queueCapacity = queueCapacity;
		}

		/**
		 * Returns the processing concurrency.
		 * @return the processing concurrency
		 */
		public int getProcessingConcurrency() {
			return processingConcurrency;
		}

		/**
		 * Sets the processing concurrency.
		 * @param processingConcurrency the processing concurrency
		 */
		public void setProcessingConcurrency(int processingConcurrency) {
			this.processingConcurrency = processingConcurrency;
		}

		/**
		 * Returns whether observation is enabled for the listener container.
		 * @return {@code true} if observation is enabled
		 */
		public boolean isObservationEnabled() {
			return this.observationEnabled;
		}

		/**
		 * Sets whether observation is enabled for the listener container.
		 * @param observationEnabled {@code true} to enable observation
		 */
		public void setObservationEnabled(boolean observationEnabled) {
			this.observationEnabled = observationEnabled;
		}
	}
}
