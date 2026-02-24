package dev.linhvu.zalobot.boot;

import dev.linhvu.zalobot.client.ZaloBotClient;

/**
 * Callback interface that can be used to customize a
 * {@link dev.linhvu.zalobot.client.ZaloBotClient.Builder ZaloBotClient.Builder}.
 *
 * @author Linh Vu
 * @since 0.0.1
 */
@FunctionalInterface
public interface ZaloBotClientCustomizer {

	/**
	 * Callback to customize a {@link dev.linhvu.zalobot.client.ZaloBotClient.Builder
	 * ZaloBotClient.Builder} instance.
	 * @param zaloBotClientBuilder the client builder to customize
	 */
	void customize(ZaloBotClient.Builder zaloBotClientBuilder);
}
