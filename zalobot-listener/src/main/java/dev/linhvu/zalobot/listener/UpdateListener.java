package dev.linhvu.zalobot.listener;

import dev.linhvu.zalobot.core.model.GetUpdatesResult;

/**
 * Callback interface for processing updates received from the Zalo Bot API.
 *
 * <p>Implementations of this interface are invoked by the listener container
 * each time a new update is received.
 *
 * @see UpdateListenerContainer
 */
@FunctionalInterface
public interface UpdateListener {

	/**
	 * Called when a new update is received.
	 *
	 * @param update the update received from the Zalo Bot API
	 */
	void onUpdate(GetUpdatesResult update);
}
