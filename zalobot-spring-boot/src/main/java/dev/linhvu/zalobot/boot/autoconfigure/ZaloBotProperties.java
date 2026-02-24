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

	public String getBotToken() {
		return this.botToken;
	}

	public void setBotToken(String botToken) {
		this.botToken = botToken;
	}

	public Client getClient() {
		return this.client;
	}

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

		public String getScheme() {
			return this.scheme;
		}

		public void setScheme(String scheme) {
			this.scheme = scheme;
		}

		public String getHost() {
			return this.host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return this.port;
		}

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
		 * Interval between poll cycles.
		 * Maps to ContainerProperties.pollInterval.
		 */
		private Duration pollInterval = Duration.ofSeconds(0);

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
		 * Number of concurrent listener containers.
		 * 1 creates a single ZaloUpdateListenerContainer.
		 * >1 creates a ConcurrentUpdateListenerContainer with this many children.
		 */
		private int concurrency = 1;

		public boolean isEnabled() {
			return this.enabled;
		}

		public void setEnabled(boolean enabled) {
			this.enabled = enabled;
		}

		public Duration getPollTimeout() {
			return this.pollTimeout;
		}

		public void setPollTimeout(Duration pollTimeout) {
			this.pollTimeout = pollTimeout;
		}

		public Duration getPollInterval() {
			return this.pollInterval;
		}

		public void setPollInterval(Duration pollInterval) {
			this.pollInterval = pollInterval;
		}

		public Duration getShutdownTimeout() {
			return this.shutdownTimeout;
		}

		public void setShutdownTimeout(Duration shutdownTimeout) {
			this.shutdownTimeout = shutdownTimeout;
		}

		public Duration getBackOffInterval() {
			return this.backOffInterval;
		}

		public void setBackOffInterval(Duration backOffInterval) {
			this.backOffInterval = backOffInterval;
		}

		public Duration getMaxBackOffInterval() {
			return this.maxBackOffInterval;
		}

		public void setMaxBackOffInterval(Duration maxBackOffInterval) {
			this.maxBackOffInterval = maxBackOffInterval;
		}

		public int getConcurrency() {
			return this.concurrency;
		}

		public void setConcurrency(int concurrency) {
			this.concurrency = concurrency;
		}
	}
}
