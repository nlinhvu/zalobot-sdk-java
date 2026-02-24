package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request object for sending a sticker message to a Zalo chat.
 *
 * <p>The compact constructor validates that both {@code chatId} and {@code sticker}
 * are non-empty.
 *
 * @param chatId the identifier of the target chat
 * @param sticker the identifier of the sticker to send
 * @see SendMessageResult
 */
public record SendSticker(
		@JsonProperty("chat_id") String chatId,
		@JsonProperty("sticker") String sticker
) {
	/**
	 * Creates a new {@code SendSticker} with validation.
	 *
	 * @throws IllegalArgumentException if {@code chatId} or {@code sticker} is null/empty
	 */
	public SendSticker {
		if (chatId == null || chatId.isEmpty()) {
			throw new IllegalArgumentException("chat_id cannot be null or empty");
		}
		if (sticker == null || sticker.isEmpty()) {
			throw new IllegalArgumentException("sticker cannot be null or empty");
		}
	}
}
