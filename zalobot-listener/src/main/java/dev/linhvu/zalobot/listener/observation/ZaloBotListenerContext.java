package dev.linhvu.zalobot.listener.observation;

import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import io.micrometer.observation.Observation;

/**
 * {@link Observation.Context} for Zalo Bot listener update processing.
 *
 * <p>Extends {@link Observation.Context} directly since listener processing
 * is a local operation without network propagation.
 *
 * @author Linh Vu
 * @since 0.0.1
 */
public class ZaloBotListenerContext extends Observation.Context {

	private final String listenerId;
	private final GetUpdatesResult update;

	/**
	 * Creates a new observation context for the given listener and update.
	 *
	 * @param listenerId the identifier of the listener container processing the update
	 * @param update the update being processed
	 */
	public ZaloBotListenerContext(String listenerId, GetUpdatesResult update) {
		this.listenerId = listenerId;
		this.update = update;
	}

	/**
	 * Returns the identifier of the listener container.
	 *
	 * @return the listener container ID
	 */
	public String getListenerId() {
		return this.listenerId;
	}

	/**
	 * Returns the update being processed.
	 *
	 * @return the {@link GetUpdatesResult} update
	 */
	public GetUpdatesResult getUpdate() {
		return this.update;
	}

	/**
	 * Returns the event name from the update, or {@code "unknown"} if not available.
	 *
	 * @return the event name
	 */
	public String getEventName() {
		return this.update.eventName() != null ? this.update.eventName() : "unknown";
	}
}
