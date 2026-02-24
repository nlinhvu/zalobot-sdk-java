package dev.linhvu.zalobot.listener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import dev.linhvu.zalobot.client.ZaloBotClient;

/**
 * Multi-threaded {@link UpdateListenerContainer} that manages multiple
 * {@link ZaloUpdateListenerContainer} children for concurrent polling.
 *
 * <p>The number of concurrent polling threads is controlled by
 * {@link #setConcurrency(int)}. Each child container runs its own independent
 * polling loop. Lifecycle operations (start, stop, pause, resume) are
 * coordinated across all children.
 *
 * @see ZaloUpdateListenerContainer
 */
public class ConcurrentUpdateListenerContainer extends AbstractUpdateListenerContainer{

	private final ZaloBotClient client;
	private final List<ZaloUpdateListenerContainer> containers = new ArrayList<>();
	private int concurrency = 1;

	public ConcurrentUpdateListenerContainer(ZaloBotClient client, ContainerProperties containerProperties) {
		super(containerProperties);
		if (client == null) {
			throw new IllegalArgumentException("'client' cannot be null");
		}
		this.client = client;
	}

	public void setConcurrency(int concurrency) {
		if (concurrency < 1) {
			throw new IllegalArgumentException("concurrency must be > 0");
		}
		this.concurrency = concurrency;
	}

	public int getConcurrency() {
		return this.concurrency;
	}

	public List<ZaloUpdateListenerContainer> getContainers() {
		this.lifecycleLock.lock();
		try {
			return List.copyOf(this.containers);
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	@Override
	protected void doStart() {
		if (!isRunning()) {
			this.containers.clear();
			setRunning(true);

			for (int i = 0; i < this.concurrency; i++) {
				ZaloUpdateListenerContainer child = constructContainer(i);
				configureChildContainer(i, child);
				child.start();
				this.containers.add(child);
			}
		}

	}

	private ZaloUpdateListenerContainer constructContainer(int index) {
		return new ZaloUpdateListenerContainer(this, this.client, getContainerProperties());
	}

	private void configureChildContainer(int index, ZaloUpdateListenerContainer child) {
		ContainerProperties childProps = child.getContainerProperties();

		UpdateListener listener = getContainerProperties().getUpdateListener();
		childProps.setUpdateListener(listener);

		Executor executor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "zalo-listener-" + index + "-C-");
			t.setDaemon(true);
			return t;
		});
		childProps.setListenerTaskExecutor(executor);

		if (isPauseRequested()) {
			child.pause();
		}
	}

	@Override
	protected void doStop(Runnable callback) {
		final AtomicInteger count = new AtomicInteger();

		if (isRunning()) {
			setRunning(false);

			for (ZaloUpdateListenerContainer container : this.containers) {
				if (container.isRunning()) {
					count.incrementAndGet();
				}
			}

			if (count.get() == 0) {
				callback.run();
				this.containers.clear();
				return;
			}

			for (ZaloUpdateListenerContainer container : this.containers) {
				if (container.isRunning()) {
					container.stop();
					if (count.decrementAndGet() <= 0) {
						callback.run();
					}
				}
			}
			this.containers.clear();
		}
	}

	@Override
	public void pause() {
		this.lifecycleLock.lock();
		try {
			super.pause();
			this.containers.forEach(AbstractUpdateListenerContainer::pause);
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	@Override
	public void resume() {
		this.lifecycleLock.lock();
		try {
			super.resume();
			this.containers.forEach(AbstractUpdateListenerContainer::resume);
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	@Override
	public boolean isContainerPaused() {
		this.lifecycleLock.lock();
		try {
			if (!isPauseRequested()) {
				return false;
			}
			for (ZaloUpdateListenerContainer container : this.containers) {
				if (!container.isContainerPaused()) {
					return false;
				}
			}
			return true;
		}
		finally {
			this.lifecycleLock.unlock();
		}
	}

	@Override
	public String toString() {
		return "ConcurrentUpdateListenerContainer [concurrency=" + this.concurrency
				+ ", running=" + isRunning() + "]";
	}
}
