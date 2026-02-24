package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of the {@code getUpdates} API method, representing a single update event.
 *
 * <p>Each update contains a message payload and an event name that identifies
 * the type of update. Use the convenience methods {@link #isTextMessage()},
 * {@link #isImageMessage()}, and {@link #isStickerMessage()} to determine
 * the event type.
 *
 * @param message the message payload associated with this update
 * @param eventName the event type identifier (e.g., {@code "message.text.received"})
 * @see GetUpdates
 */
public record GetUpdatesResult(
        Message message,
        @JsonProperty("event_name") String eventName
) {

    /**
     * Returns {@code true} if this update is a text message event.
     *
     * @return {@code true} if the event name is {@code "message.text.received"}
     */
    public boolean isTextMessage() {
        return "message.text.received".equals(eventName);
    }

    /**
     * Returns {@code true} if this update is an image message event.
     *
     * @return {@code true} if the event name is {@code "message.image.received"}
     */
    public boolean isImageMessage() {
        return "message.image.received".equals(eventName);
    }

    /**
     * Returns {@code true} if this update is a sticker message event.
     *
     * @return {@code true} if the event name is {@code "message.sticker.received"}
     */
    public boolean isStickerMessage() {
        return "message.sticker.received".equals(eventName);
    }

    /**
     * Represents the message content within an update.
     *
     * @param chat the chat where the message was sent
     * @param text the text content of the message, or {@code null} for non-text messages
     * @param messageId the unique identifier of the message
     * @param date the timestamp of the message in Unix epoch seconds
     * @param from the sender of the message
     */
    public record Message(
        Chat chat,
        String text,
        @JsonProperty("message_id") String messageId,
        long date,
        From from
    ) {}

    /**
     * Represents the chat (conversation) where a message was sent.
     *
     * @param id the unique identifier of the chat
     * @param chatType the type of chat (e.g., "private", "group")
     */
    public record Chat(
        String id,
        @JsonProperty("chat_type") String chatType
    ) {}

    /**
     * Represents the sender of a message.
     *
     * @param id the unique identifier of the sender
     * @param isBot whether the sender is a bot
     * @param displayName the display name of the sender
     */
    public record From(
        String id,
        @JsonProperty("is_bot") boolean isBot,
        @JsonProperty("display_name") String displayName
    ) {}
}
