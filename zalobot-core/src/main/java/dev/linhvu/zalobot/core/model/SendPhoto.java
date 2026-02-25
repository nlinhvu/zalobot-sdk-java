package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request object for sending a photo message to a Zalo chat.
 *
 * <p>The compact constructor validates that both {@code chatId} and {@code photo}
 * are non-empty. The {@code caption} is optional and may be {@code null}.
 *
 * @param chatId the identifier of the target chat
 * @param caption an optional caption for the photo, or {@code null}
 * @param photo the URL or identifier of the photo to send
 * @author Linh Vu
 * @since 0.0.1
 * @see SendMessageResult
 */
public record SendPhoto(
		@JsonProperty("chat_id") String chatId,
		@JsonProperty("caption") String caption,
		@JsonProperty("photo") String photo
) {
	/**
	 * Creates a new {@code SendPhoto} with validation.
	 *
	 * @throws IllegalArgumentException if {@code chatId} or {@code photo} is null/empty
	 */
	public SendPhoto {
		if (chatId == null || chatId.isEmpty()) {
			throw new IllegalArgumentException("chat_id cannot be null or empty");
		}
		if (photo == null || photo.isEmpty()) {
			throw new IllegalArgumentException("photo cannot be null or empty");
		}
	}
}
