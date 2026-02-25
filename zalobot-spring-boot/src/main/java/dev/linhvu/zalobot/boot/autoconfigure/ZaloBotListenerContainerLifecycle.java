package dev.linhvu.zalobot.boot.autoconfigure;

import dev.linhvu.zalobot.listener.UpdateListenerContainer;

import org.springframework.context.SmartLifecycle;

/**
 * Adapter that bridges an {@link UpdateListenerContainer} into Spring's
 * {@link SmartLifecycle} mechanism.
 *
 * <p>Configured to auto-start ({@link #isAutoStartup()} returns {@code true})
 * and runs at a late phase ({@code Integer.MAX_VALUE - 100}) to ensure the
 * listener starts after all other beans are initialized.
 */
class ZaloBotListenerContainerLifecycle implements SmartLifecycle {

	private final UpdateListenerContainer container;

	ZaloBotListenerContainerLifecycle(UpdateListenerContainer container) {
		this.container = container;
	}

	@Override
	public void start() {
		this.container.start();
	}

	@Override
	public void stop() {
		this.container.stop();
	}

	@Override
	public void stop(Runnable callback) {
		this.container.stop(callback);
	}

	@Override
	public boolean isRunning() {
		return this.container.isRunning();
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public int getPhase() {
		return Integer.MAX_VALUE - 100;
	}
}
