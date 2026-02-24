package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request object for sending a text message to a Zalo chat.
 *
 * <p>The compact constructor validates that both {@code chat_id} and {@code text}
 * are non-empty, and that the text does not exceed 2000 characters.
 *
 * @param chat_id the identifier of the target chat
 * @param text the text content to send (maximum 2000 characters)
 * @see SendMessageResult
 */
public record SendMessage(
		@JsonProperty("chat_id") String chat_id,
		@JsonProperty("text") String text
) {
	/**
	 * Creates a new {@code SendMessage} with validation.
	 *
	 * @throws IllegalArgumentException if {@code chat_id} or {@code text} is null/empty,
	 *                                  or if {@code text} exceeds 2000 characters
	 */
	public SendMessage {
		if (chat_id == null || chat_id.isEmpty()) {
			throw new IllegalArgumentException("chat_id cannot be null or empty");
		}
		if (text == null || text.isEmpty()) {
			throw new IllegalArgumentException("text cannot be null or empty");
		}
		if (text.length() > 2000) {
			throw new IllegalArgumentException("text cannot exceed 2000 characters");
		}
	}
}
