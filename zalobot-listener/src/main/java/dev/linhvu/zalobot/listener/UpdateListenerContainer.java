package dev.linhvu.zalobot.listener;

/**
 * Lifecycle interface for a container that manages the polling and dispatching
 * of Zalo Bot API updates to an {@link UpdateListener}.
 *
 * <p>Supports start/stop lifecycle management as well as pause/resume
 * functionality. Implements {@link AutoCloseable} for use in try-with-resources.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloBotUpdateListenerContainer
 */
public interface UpdateListenerContainer extends AutoCloseable {

	/**
	 * Sets the update listener that will receive polled updates.
	 *
	 * @param updateListener the listener to register
	 */
	void setUpdateListener(UpdateListener updateListener);

	/**
	 * Starts the container, beginning the polling loop.
	 */
	void start();

	/**
	 * Stops the container, waiting for graceful shutdown up to the configured timeout.
	 */
	void stop();

	/**
	 * Returns whether this container is currently running.
	 *
	 * @return {@code true} if the container is running
	 */
	boolean isRunning();

	/**
	 * Requests the container to pause polling. The container may not pause
	 * immediately; use {@link #isContainerPaused()} to check actual state.
	 */
	void pause();

	/**
	 * Resumes polling after a pause.
	 */
	void resume();

	/**
	 * Returns whether a pause has been requested.
	 *
	 * @return {@code true} if a pause has been requested
	 */
	boolean isPauseRequested();

	/**
	 * Returns whether the container has actually paused its polling loop.
	 *
	 * @return {@code true} if the container is fully paused
	 */
	boolean isContainerPaused();

	/**
	 * Returns the container configuration properties.
	 *
	 * @return the container properties
	 */
	ContainerProperties getContainerProperties();

	/**
	 * Stops the container asynchronously. The callback is invoked after the
	 * container has fully stopped. If the container is not running, the
	 * callback is invoked immediately.
	 *
	 * @param callback invoked when the container has stopped
	 */
	default void stop(Runnable callback) {
		stop();
		callback.run();
	}

	/**
	 * Stops the container. Equivalent to calling {@link #stop()}.
	 */
	@Override
	default void close() {
		stop();
	}
}
