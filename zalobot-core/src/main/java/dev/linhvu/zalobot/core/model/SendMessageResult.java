package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result returned after successfully sending a message, photo, or sticker.
 *
 * @param messageId the unique identifier assigned to the sent message
 * @param date the timestamp of the sent message in Unix epoch seconds
 * @author Linh Vu
 * @since 0.0.1
 */
public record SendMessageResult(
		@JsonProperty("message_id") String messageId,
		long date
) {}
