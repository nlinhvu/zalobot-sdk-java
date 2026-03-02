package dev.linhvu.zalobot.client.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import org.jspecify.annotations.Nullable;

/**
 * {@link ObservationConvention} for Zalo Bot API client observations.
 *
 * <p>Implement this interface to customize the observation name and key values
 * produced for outgoing API requests. A custom implementation can be registered
 * via {@link dev.linhvu.zalobot.client.ZaloBotClient.Builder#observationConvention}.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloBotClientContext
 */
public interface ZaloBotClientObservationConvention extends ObservationConvention<ZaloBotClientContext> {

	@Override
	default boolean supportsContext(Observation.Context context) {
		return context instanceof ZaloBotClientContext;
	}

	@Override
	@Nullable
	default String getName() {
		return "zalobot.client.requests";
	}
}
