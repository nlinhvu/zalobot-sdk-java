package dev.linhvu.zalobot.client.observation;

import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.ClientHttpResponse;
import dev.linhvu.zalobot.client.http.HttpMethod;
import io.micrometer.common.KeyValue;
import io.micrometer.observation.Observation;
import io.micrometer.observation.transport.RequestReplySenderContext;

/**
 * {@link Observation.Context} for Zalo Bot API client HTTP requests.
 *
 * <p>Extends {@link RequestReplySenderContext} to support distributed
 * tracing header propagation into outgoing HTTP requests.
 *
 * @author Linh Vu
 * @since 0.0.1
 */
public class ZaloBotClientContext extends RequestReplySenderContext<ClientHttpRequest, ClientHttpResponse> {

	private final String methodPath;
	private final HttpMethod httpMethod;
	private String exceptionName = KeyValue.NONE_VALUE;
	private boolean success = true;


	/**
	 * Creates a new observation context for the given API method.
	 *
	 * @param methodPath the Zalo Bot API method path (e.g., "sendMessage")
	 * @param httpMethod the HTTP method used for the request
	 */
	public ZaloBotClientContext(String methodPath, HttpMethod httpMethod) {
		// Propagator.Setter: injects trace headers into ClientHttpRequest headers
		super((request, key, value) -> {
			if (request != null) {
				request.getHeaders().put(key, value);
			}
		});
		this.methodPath = methodPath;
		this.httpMethod = httpMethod;
		setRemoteServiceName("Zalo Bot API");
	}

	/**
	 * Returns the Zalo Bot API method path (e.g., "sendMessage", "getUpdates").
	 *
	 * @return the API method path
	 */
	public String getMethodPath() {
		return this.methodPath;
	}

	/**
	 * Returns the HTTP method used for this request.
	 *
	 * @return the HTTP method
	 */
	public HttpMethod getHttpMethod() {
		return this.httpMethod;
	}

	/**
	 * Returns whether the API request completed successfully.
	 *
	 * @return {@code true} if the request succeeded, {@code false} otherwise
	 */
	public boolean isSuccess() {
		return this.success;
	}

	/**
	 * Sets whether the API request completed successfully.
	 *
	 * @param success {@code true} if successful, {@code false} otherwise
	 */
	public void setSuccess(boolean success) {
		this.success = success;
	}

	/**
	 * Returns the name of the exception that occurred, or
	 * {@link KeyValue#NONE_VALUE} if no exception was thrown.
	 *
	 * @return the exception class name, or the "none" sentinel
	 */
	public String getExceptionName() {
		return this.exceptionName;
	}

	/**
	 * Sets the name of the exception that occurred during the request.
	 *
	 * @param exceptionName the exception class name
	 */
	public void setExceptionName(String exceptionName) {
		this.exceptionName = exceptionName;
	}

	/**
	 * Returns the full request URL, or {@code "unknown"} if the carrier
	 * has not been set.
	 *
	 * @return the request URL string
	 */
	public String getRequestUrl() {
		ClientHttpRequest request = getCarrier();
		return request != null ? request.getURI().toString() : "unknown";
	}
}
