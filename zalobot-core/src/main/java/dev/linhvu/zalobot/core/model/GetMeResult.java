package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Result of the {@code getMe} API method, containing bot account information.
 *
 * @param id the unique identifier of the bot
 * @param accountName the display name of the bot account
 * @param accountType the type of the account (e.g., "Official Account")
 * @param canJoinGroups whether the bot is allowed to join group chats
 * @author Linh Vu
 * @since 0.0.1
 * @see GetMe
 */
public record GetMeResult(
    String id,
    @JsonProperty("account_name") String accountName,
    @JsonProperty("account_type") String accountType,
    @JsonProperty("can_join_groups") boolean canJoinGroups
) {}
