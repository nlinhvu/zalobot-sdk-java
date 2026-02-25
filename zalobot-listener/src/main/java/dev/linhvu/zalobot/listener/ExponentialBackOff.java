package dev.linhvu.zalobot.listener;

import java.time.Duration;

/**
 * Implements an exponential backoff strategy for retrying failed operations.
 *
 * <p>The interval starts at the initial value and multiplies by the given factor
 * (default 2.0) on each call to {@link #nextBackOffMillis()}, up to the
 * configured maximum. Call {@link #reset()} to restart from the initial interval.
 *
 * @author Linh Vu
 * @since 0.0.1
 */
public class ExponentialBackOff {

	private final long initialIntervalMillis;
	private final long maxIntervalMillis;
	private final double multiplier;

	private long currentIntervalMillis;

	/**
	 * Creates a new exponential backoff with a default multiplier of 2.0.
	 *
	 * @param initialInterval the initial backoff interval
	 * @param maxInterval the maximum backoff interval
	 */
	public ExponentialBackOff(Duration initialInterval, Duration maxInterval) {
		this(initialInterval, maxInterval, 2.0);
	}

	/**
	 * Creates a new exponential backoff with the given parameters.
	 *
	 * @param initialInterval the initial backoff interval
	 * @param maxInterval the maximum backoff interval
	 * @param multiplier the factor by which the interval increases each time
	 */
	public ExponentialBackOff(Duration initialInterval, Duration maxInterval, double multiplier) {
		this.initialIntervalMillis = initialInterval.toMillis();
		this.maxIntervalMillis = maxInterval.toMillis();
		this.multiplier = multiplier;
		this.currentIntervalMillis = this.initialIntervalMillis;
	}

	/**
	 * Returns the next backoff interval in milliseconds and advances the internal state.
	 *
	 * @return the backoff interval in milliseconds
	 */
	public long nextBackOffMillis() {
		long next = this.currentIntervalMillis;
		this.currentIntervalMillis = Math.min(
				(long) (this.currentIntervalMillis * this.multiplier),
				this.maxIntervalMillis);
		return next;
	}

	/**
	 * Resets the backoff to the initial interval.
	 */
	public void reset() {
		this.currentIntervalMillis = this.initialIntervalMillis;
	}
}
