package dev.linhvu.zalobot.client.observation;

import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.common.docs.KeyName;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationConvention;
import io.micrometer.observation.docs.ObservationDocumentation;
import org.jspecify.annotations.Nullable;

/**
 * {@link ObservationDocumentation} for Zalo Bot client operations.
 *
 * @author Linh Vu
 * @since 0.1.0
 */
public enum ZaloBotClientObservation implements ObservationDocumentation {

	API_REQUEST {
		@Override
		public @Nullable Class<? extends ObservationConvention<? extends Observation.Context>> getDefaultConvention() {
			return DefaultZaloBotClientObservationConvention.class;
		}

		@Override
		public KeyName[] getLowCardinalityKeyNames() {
			return LowCardinalityKeyNames.values();
		}

		@Override
		public KeyName[] getHighCardinalityKeyNames() {
			return HighCardinalityKeyNames.values();
		}
	};

	/**
	 * Low-cardinality tag names (safe for metrics).
	 */
	public enum LowCardinalityKeyNames implements KeyName {
		/**
		 * Zalo Bot API method path (e.g., "sendMessage", "getUpdates").
		 */
		METHOD {
			@Override
			public String asString() {
				return "zalobot.method";
			}
		},

		/**
		 * Request outcome: "SUCCESS" or "ERROR".
		 */
		OUTCOME {
			@Override
			public String asString() {
				return "outcome";
			}
		},

		/**
		 * Exception class name or "none".
		 */
		EXCEPTION {
			@Override
			public String asString() {
				return "exception";
			}
		}
	}

	/**
	 * High-cardinality tag names (traces only).
	 */
	public enum HighCardinalityKeyNames implements KeyName {

		/**
		 * Full request URL.
		 */
		REQUEST_URL {
			@Override
			public String asString() {
				return "zalobot.request.url";
			}
		}
	}

	/**
	 * Default {@link ZaloBotClientObservationConvention}.
	 */
	public static class DefaultZaloBotClientObservationConvention implements ZaloBotClientObservationConvention {

		public static final DefaultZaloBotClientObservationConvention INSTANCE =
				new DefaultZaloBotClientObservationConvention();

		private static final KeyValue EXCEPTION_NONE = KeyValue.of(LowCardinalityKeyNames.EXCEPTION, KeyValue.NONE_VALUE);
		private static final KeyValue OUTCOME_SUCCESS = KeyValue.of(LowCardinalityKeyNames.OUTCOME, "SUCCESS");
		private static final KeyValue OUTCOME_ERROR = KeyValue.of(LowCardinalityKeyNames.OUTCOME, "ERROR");

		@Override
		public @Nullable String getContextualName(ZaloBotClientContext context) {
			return context.getMethodPath();
		}

		@Override
		public KeyValues getLowCardinalityKeyValues(ZaloBotClientContext context) {
			return KeyValues.of(
					method(context),
					outcome(context),
					exception(context)
			);
		}

		@Override
		public KeyValues getHighCardinalityKeyValues(ZaloBotClientContext context) {
			return KeyValues.of(
					KeyValue.of("zalobot.request.url", context.getRequestUrl())
			);
		}

		private KeyValue method(ZaloBotClientContext context) {
			return KeyValue.of(LowCardinalityKeyNames.METHOD, context.getMethodPath());
		}

		private KeyValue outcome(ZaloBotClientContext context) {
			return context.isSuccess() ? OUTCOME_SUCCESS : OUTCOME_ERROR;
		}

		private KeyValue exception(ZaloBotClientContext context) {
			Throwable error = context.getError();
			if (error != null) {
				String simpleName = error.getClass().getSimpleName();
				return KeyValue.of(LowCardinalityKeyNames.EXCEPTION,
						simpleName.isEmpty() ? error.getClass().getName() : simpleName);
			}
			return EXCEPTION_NONE;
		}

	}
}
