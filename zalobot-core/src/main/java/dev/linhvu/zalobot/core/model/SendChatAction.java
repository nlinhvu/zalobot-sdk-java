package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request object for sending a chat action (e.g., typing indicator) to a Zalo chat.
 *
 * <p>Chat actions inform the user that the bot is performing an activity such as
 * typing or uploading a photo. Use the factory methods {@link #typing(String)}
 * and {@link #uploadPhoto(String)} for convenience.
 *
 * @param chatId the identifier of the target chat
 * @param action the action type (e.g., {@code "typing"}, {@code "upload_photo"})
 * @author Linh Vu
 * @since 0.0.1
 * @see SendMessageResult
 */
public record SendChatAction(
    @JsonProperty("chat_id") String chatId,
    @JsonProperty("action") String action
) {
    /**
     * Creates a typing indicator action for the given chat.
     *
     * @param chatId the identifier of the target chat
     * @return a new {@code SendChatAction} with the {@code "typing"} action
     */
    public static SendChatAction typing(String chatId) {
        return new SendChatAction(chatId, "typing");
    }

    /**
     * Creates an upload photo indicator action for the given chat.
     *
     * @param chatId the identifier of the target chat
     * @return a new {@code SendChatAction} with the {@code "upload_photo"} action
     */
    public static SendChatAction uploadPhoto(String chatId) {
        return new SendChatAction(chatId, "upload_photo");
    }
}
