package dev.linhvu.zalobot.listener;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Abstract base class for {@link UpdateListenerContainer} implementations.
 *
 * <p>Provides thread-safe lifecycle management (start/stop/pause/resume) using
 * a {@link ReentrantLock}. Subclasses implement the actual start and stop
 * logic via {@link #doStart()} and {@link #doStop(Runnable)}.
 *
 * @author Linh Vu
 * @since 0.0.1
 */
public abstract class AbstractUpdateListenerContainer implements UpdateListenerContainer {

	protected final ReentrantLock lifecycleLock = new ReentrantLock();
	private final ContainerProperties containerProperties;
	private volatile boolean running = false;
	private volatile boolean paused = false;

	/**
	 * Creates a new container with the given properties.
	 * @param containerProperties the container configuration (must not be {@code null})
	 * @throws IllegalArgumentException if {@code containerProperties} is {@code null}
	 */
	protected AbstractUpdateListenerContainer(ContainerProperties containerProperties) {
		if (containerProperties == null) {
			throw new IllegalArgumentException("'containerProperties' cannot be null");
		}
		this.containerProperties = containerProperties;
	}

	@Override
	public final void start() {
		this.lifecycleLock.lock();
		try {
			if (!isRunning()) {
				if (this.containerProperties.getUpdateListener() == null) {
					throw new IllegalStateException("An UpdateListener must be provided via setupUpdateListener()");
				}
				doStart();
			}
		} finally {
			this.lifecycleLock.unlock();
		}
	}

	/**
	 * Performs the actual start logic. Called by {@link #start()} while holding
	 * the lifecycle lock and only if the container is not already running.
	 */
	protected abstract void doStart();

	@Override
	public final void stop() {
		if (isRunning()) {
			final CountDownLatch latch = new CountDownLatch(1);

			this.lifecycleLock.lock();
			try {
				doStop(latch::countDown);
			} finally {
				this.lifecycleLock.unlock();
			}

			try {
				latch.await(this.containerProperties.getShutdownTimeout().toMillis(), TimeUnit.MILLISECONDS);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}
	}

	@Override
	public void stop(Runnable callback) {
		this.lifecycleLock.lock();
		try {
			if (isRunning()) {
				doStop(callback);
			}
			else {
				callback.run();
			}
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	/**
	 * Performs the actual stop logic. Called by {@link #stop()} and
	 * {@link #stop(Runnable)} while holding the lifecycle lock.
	 * @param callback a callback to invoke once the container has stopped
	 */
	protected abstract void doStop(Runnable callback);

	@Override
	public boolean isRunning() {
		return this.running;
	}

	/**
	 * Sets the running state of this container.
	 * @param running {@code true} if the container is running
	 */
	protected void setRunning(boolean running) {
		this.running = running;
	}

	@Override
	public void pause() {
		this.paused = true;
	}

	@Override
	public void resume() {
		this.paused = false;
	}

	@Override
	public boolean isPauseRequested() {
		return this.paused;
	}

	@Override
	public boolean isContainerPaused() {
		return this.paused;
	}

	@Override
	public ContainerProperties getContainerProperties() {
		return this.containerProperties;
	}

	@Override
	public void setUpdateListener(UpdateListener updateListener) {
		if (updateListener == null) {
			throw new IllegalArgumentException("'updateListener' cannot be null");
		}
		this.containerProperties.setUpdateListener(updateListener);
	}
}
