package dev.linhvu.zalobot.client.http;

/**
 * Represents an HTTP request method.
 *
 * <p>Pre-defined constants are provided for all standard HTTP methods.
 * Custom methods can be obtained via {@link #valueOf(String)}.
 */
public final class HttpMethod {

	/** HTTP GET method. */
	public static final HttpMethod GET = new HttpMethod("GET");
	/** HTTP HEAD method. */
	public static final HttpMethod HEAD = new HttpMethod("HEAD");
	/** HTTP POST method. */
	public static final HttpMethod POST = new HttpMethod("POST");
	/** HTTP PUT method. */
	public static final HttpMethod PUT = new HttpMethod("PUT");
	/** HTTP PATCH method. */
	public static final HttpMethod PATCH = new HttpMethod("PATCH");
	/** HTTP DELETE method. */
	public static final HttpMethod DELETE = new HttpMethod("DELETE");
	/** HTTP OPTIONS method. */
	public static final HttpMethod OPTIONS = new HttpMethod("OPTIONS");
	/** HTTP TRACE method. */
	public static final HttpMethod TRACE = new HttpMethod("TRACE");

	private final String name;

	private HttpMethod(String name) {
		this.name = name;
	}

	/**
	 * Returns an {@code HttpMethod} for the given method name.
	 * Standard methods return the shared constant; non-standard methods
	 * create a new instance.
	 *
	 * @param method the HTTP method name (e.g., {@code "GET"}, {@code "POST"})
	 * @return the corresponding {@code HttpMethod}
	 */
	public static HttpMethod valueOf(String method) {
		return switch (method) {
			case "GET" -> GET;
			case "HEAD" -> HEAD;
			case "POST" -> POST;
			case "PUT" -> PUT;
			case "PATCH" -> PATCH;
			case "DELETE" -> DELETE;
			case "OPTIONS" -> OPTIONS;
			case "TRACE" -> TRACE;
			default -> new HttpMethod(method);
		};
	}

	/**
	 * Returns the name of this HTTP method.
	 *
	 * @return the method name (e.g., {@code "GET"}, {@code "POST"})
	 */
	public String name() {
		return this.name;
	}

	/**
	 * Returns {@code true} if this method matches the given method name.
	 *
	 * @param method the method name to compare
	 * @return {@code true} if the names are equal
	 */
	public boolean matches(String method) {
		return name().equals(method);
	}
}
