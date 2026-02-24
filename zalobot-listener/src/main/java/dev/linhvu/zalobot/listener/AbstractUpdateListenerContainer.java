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
 */
public abstract class AbstractUpdateListenerContainer implements UpdateListenerContainer {

	protected final ReentrantLock lifecycleLock = new ReentrantLock();
	private final ContainerProperties containerProperties;
	private volatile boolean running = false;
	private volatile boolean paused = false;

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

	protected abstract void doStop(Runnable callback);

	@Override
	public boolean isRunning() {
		return this.running;
	}

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
