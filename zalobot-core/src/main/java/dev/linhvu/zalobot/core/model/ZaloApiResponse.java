package dev.linhvu.zalobot.core.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Generic wrapper for all Zalo Bot API responses.
 *
 * <p>Every response from the Zalo Bot API follows a common structure containing
 * a success indicator, an optional result payload, and an error code when the
 * request fails.
 *
 * @param <T> the type of the result payload
 * @param ok whether the API request was successful
 * @param result the result payload, or {@code null} if the request failed
 * @param errorCode the error code returned by the API; {@code 0} indicates success
 * @author Linh Vu
 * @since 0.0.1
 */
public record ZaloApiResponse<T>(
		boolean ok,
		T result,
		@JsonProperty("error_code") int errorCode
) {}