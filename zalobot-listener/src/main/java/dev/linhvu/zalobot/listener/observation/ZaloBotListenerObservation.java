package dev.linhvu.zalobot.listener.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;
import org.jspecify.annotations.Nullable;

/**
 * {@link ObservationDocumentation} for Zalo Bot listener operations.
 */
public enum ZaloBotListenerObservation implements ObservationDocumentation {

	/**
	 * Observation for processing a received update.
	 */
	LISTENER_OBSERVATION {

		@Override
		public @Nullable Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
			return DefaultZaloBotListenerObservationConvention.class;
		}

		@Override
		public KeyName[] getLowCardinalityKeyNames() {
			return LowCardinalityKeyNames.values();
		}
	};

	public enum LowCardinalityKeyNames implements KeyName {

		LISTENER_ID {
			@Override
			public String asString() {
				return "zalobot.listener.id";
			}
		},

		EVENT_NAME {
			@Override
			public String asString() {
				return "zalobot.event.name";
			}
		},

		EXCEPTION {
			@Override
			public String asString() {
				return "exception";
			}
		}
	}

	public static class DefaultZaloBotListenerObservationConvention
			implements ZaloBotListenerObservationConvention {

		public static final DefaultZaloBotListenerObservationConvention INSTANCE =
				new DefaultZaloBotListenerObservationConvention();

		@Override
		public String getContextualName(ZaloBotListenerContext context) {
			return context.getEventName() + " process";
		}

		@Override
		public KeyValues getLowCardinalityKeyValues(ZaloBotListenerContext context) {
			return KeyValues.of(
					KeyValue.of(LowCardinalityKeyNames.LISTENER_ID, context.getListenerId()),
					KeyValue.of(LowCardinalityKeyNames.EVENT_NAME, context.getEventName()),
					exception(context)
			);
		}

		private KeyValue exception(ZaloBotListenerContext context) {
			Throwable error = context.getError();
			if (error != null) {
				return KeyValue.of(LowCardinalityKeyNames.EXCEPTION,
						error.getClass().getSimpleName());
			}
			return KeyValue.of(LowCardinalityKeyNames.EXCEPTION, KeyValue.NONE_VALUE);
		}
	}
}
