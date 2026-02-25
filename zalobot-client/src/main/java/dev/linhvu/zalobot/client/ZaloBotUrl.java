package dev.linhvu.zalobot.client;

/**
 * Immutable URL configuration for the Zalo Bot API endpoint.
 *
 * <p>Encapsulates the scheme, host, and port used to construct API request URIs.
 * The {@link #DEFAULT_URL} constant provides the default production endpoint.
 *
 * @param scheme the URL scheme (e.g., {@code "https"})
 * @param host the API hostname
 * @param port the port number
 * @author Linh Vu
 * @since 0.0.1
 */
public record ZaloBotUrl(
		String scheme,
		String host,
		int port
) {
	/** Default Zalo Bot API URL: {@code https://bot-api.zaloplatforms.com:443}. */
	public static final ZaloBotUrl DEFAULT_URL = new ZaloBotUrl("https", "bot-api.zaloplatforms.com", 443);
}
