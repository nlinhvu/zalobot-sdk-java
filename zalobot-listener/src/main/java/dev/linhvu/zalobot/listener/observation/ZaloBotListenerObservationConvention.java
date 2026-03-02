package dev.linhvu.zalobot.listener.observation;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import org.jspecify.annotations.Nullable;

/**
 * {@link ObservationConvention} for Zalo Bot listener observations.
 *
 * <p>Implement this interface to customize the observation name and key values
 * produced during update processing. A custom implementation can be registered
 * via {@link dev.linhvu.zalobot.listener.ContainerProperties#setObservationConvention}.
 *
 * @author Linh Vu
 * @since 0.0.1
 * @see ZaloBotListenerContext
 */
public interface ZaloBotListenerObservationConvention extends ObservationConvention<ZaloBotListenerContext> {

	@Override
	default boolean supportsContext(Observation.Context context) {
		return context instanceof ZaloBotListenerContext;
	}

	@Override
	@Nullable
	default String getName() {
		return "zalobot.listener";
	}
}
