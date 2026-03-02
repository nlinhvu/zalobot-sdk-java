package dev.linhvu.zalobot.client;

import dev.linhvu.zalobot.client.http.ClientHttpRequestFactory;
import dev.linhvu.zalobot.client.observation.ZaloBotClientObservationConvention;
import dev.linhvu.zalobot.core.model.GetMe;
import dev.linhvu.zalobot.core.model.GetMeResult;
import dev.linhvu.zalobot.core.model.GetUpdates;
import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import dev.linhvu.zalobot.core.model.SendChatAction;
import dev.linhvu.zalobot.core.model.SendMessage;
import dev.linhvu.zalobot.core.model.SendMessageResult;
import dev.linhvu.zalobot.core.model.SendPhoto;
import dev.linhvu.zalobot.core.model.SendSticker;
import dev.linhvu.zalobot.core.model.ZaloApiResponse;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.ObservationRegistry;
import tools.jackson.databind.json.JsonMapper;

/**
 * Main interface for interacting with the Zalo Bot API.
 *
 * <p>Provides a fluent API for building and executing requests to the Zalo Bot API.
 * Each method returns a {@link RequestBodySpec} that allows setting request headers,
 * body, and executing the request.
 *
 * <p>Create instances using the {@link #builder()} method or the convenience
 * {@link #botToken(String)} shortcut:
 * <pre>{@code
 * ZaloBotClient client = ZaloBotClient.builder()
 *     .botToken("your-bot-token")
 *     .build();
 *
 * // Or simply:
 * ZaloBotClient client = ZaloBotClient.botToken("your-bot-token");
 * }</pre>
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloBotUrl
 */
public interface ZaloBotClient {

	/**
	 * Initiates a request to retrieve bot account information.
	 *
	 * @return a request spec for the getMe API method
	 */
	RequestBodySpec<GetMe, GetMeResult> getMe();

	/**
	 * Initiates a request to poll for new updates using long-polling.
	 *
	 * @return a request spec for the getUpdates API method
	 */
	RequestBodySpec<GetUpdates, GetUpdatesResult> getUpdates();

	/**
	 * Initiates a request to send a text message.
	 *
	 * @return a request spec for the sendMessage API method
	 */
	RequestBodySpec<SendMessage, SendMessageResult> sendMessage();

	/**
	 * Initiates a request to send a photo message.
	 *
	 * @return a request spec for the sendPhoto API method
	 */
	RequestBodySpec<SendPhoto, SendMessageResult> sendPhoto();

	/**
	 * Initiates a request to send a sticker message.
	 *
	 * @return a request spec for the sendSticker API method
	 */
	RequestBodySpec<SendSticker, SendMessageResult> sendSticker();

	/**
	 * Initiates a request to send a chat action (e.g., typing indicator).
	 *
	 * @return a request spec for the sendChatAction API method
	 */
	RequestBodySpec<SendChatAction, SendMessageResult> sendChatAction();

	/**
	 * Creates a new {@link Builder} for constructing a {@code ZaloBotClient}.
	 *
	 * @return a new builder instance
	 */
	static ZaloBotClient.Builder builder() {
		return new DefaultZaloBotClientBuilder();
	}

	/**
	 * Convenience method to create a {@code ZaloBotClient} with just a bot token,
	 * using default settings for all other configuration.
	 *
	 * @param botToken the Zalo bot token
	 * @return a fully configured client instance
	 */
	static ZaloBotClient botToken(String botToken) {
		return new DefaultZaloBotClientBuilder().botToken(botToken).build();
	}

	/**
	 * Builder for constructing {@link ZaloBotClient} instances with custom configuration.
	 */
	interface Builder {
		/**
		 * Sets the Zalo Bot API URL.
		 *
		 * @param url the API URL configuration
		 * @return this builder for chaining
		 */
		Builder zaloBotUrl(ZaloBotUrl url);

		/**
		 * Sets the bot token for authentication.
		 *
		 * @param botToken the Zalo bot token (required)
		 * @return this builder for chaining
		 */
		Builder botToken(String botToken);

		/**
		 * Sets a custom HTTP request factory for the client.
		 *
		 * @param requestFactory the factory to use for creating HTTP requests
		 * @return this builder for chaining
		 */
		Builder requestFactory(ClientHttpRequestFactory requestFactory);

		/**
		 * Sets a custom Jackson {@link JsonMapper} for JSON serialization/deserialization.
		 *
		 * @param jsonMapper the JSON mapper to use
		 * @return this builder for chaining
		 */
		Builder jsonMapper(JsonMapper jsonMapper);

		Builder observationRegistry(ObservationRegistry observationRegistry);
		Builder observationConvention(ZaloBotClientObservationConvention observationConvention);

		/**
		 * Builds a new {@link ZaloBotClient} with the configured settings.
		 *
		 * @return a new client instance
		 * @throws IllegalArgumentException if required parameters are missing
		 */
		ZaloBotClient build();
	}

	/**
	 * Specification for adding HTTP headers to a request.
	 *
	 * @param <S> the concrete type of this spec (for fluent chaining)
	 */
	interface RequestHeadersSpec<S extends RequestHeadersSpec<S>> {
		/**
		 * Adds an HTTP header to the request.
		 *
		 * @param headerName the header name
		 * @param headerValue the header value
		 * @return this spec for chaining
		 */
		S header(String headerName, String headerValue);
	}

	/**
	 * Specification for setting the request body and retrieving the response.
	 *
	 * @param <M> the request body type
	 * @param <N> the expected response result type
	 */
	interface RequestBodySpec<M, N> extends RequestHeadersSpec<RequestBodySpec<M, N>> {
		/**
		 * Sets the request body.
		 *
		 * @param body the request body object to serialize as JSON
		 * @return this spec for chaining
		 */
		RequestBodySpec<M, N> body(M body);

		/**
		 * Finalizes the request and prepares it for execution.
		 *
		 * @return a response spec for executing the request
		 */
		ResponseSpec<N> retrieve();
	}

	/**
	 * Specification for executing a prepared request and obtaining the response.
	 *
	 * @param <N> the expected response result type
	 */
	interface ResponseSpec<N> {
		/**
		 * Executes the HTTP request and deserializes the response.
		 *
		 * @param clazz the class of the expected result type
		 * @return the deserialized API response
		 * @throws dev.linhvu.zalobot.client.exception.ZaloBotApiException if the API returns an error
		 * @throws dev.linhvu.zalobot.client.exception.ZaloBotClientException if an HTTP error occurs
		 * @throws dev.linhvu.zalobot.client.exception.ZaloBotSerializationException if serialization fails
		 */
		ZaloApiResponse<N> call(Class<N> clazz);
	}
}
