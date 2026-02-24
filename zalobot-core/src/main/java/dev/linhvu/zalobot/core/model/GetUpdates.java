package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request object for the {@code getUpdates} API method.
 *
 * <p>Used to poll for new updates (messages, events) from the Zalo Bot API.
 * Supports long-polling by specifying a timeout value.
 *
 * @param timeout the long-polling timeout in seconds; the server will hold the
 *                connection open for up to this duration before returning an
 *                empty response if no updates are available. May be {@code null}
 *                for immediate (non-blocking) polling.
 * @see GetUpdatesResult
 */
public record GetUpdates(
		@JsonProperty("timeout") Long timeout
) {}
