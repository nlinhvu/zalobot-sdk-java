package dev.linhvu.zalobot.listener.observation;

import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ZaloBotListenerObservationTests {

	// ── ObservationDocumentation ──────────────────────────────────────

	@Test
	void listenerObservation_defaultConventionClass() {
		assertThat(ZaloBotListenerObservation.LISTENER_OBSERVATION.getDefaultConvention())
				.isEqualTo(ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention.class);
	}

	@Test
	void listenerObservation_lowCardinalityKeyNames() {
		assertThat(ZaloBotListenerObservation.LISTENER_OBSERVATION.getLowCardinalityKeyNames())
				.containsExactly(ZaloBotListenerObservation.LowCardinalityKeyNames.values());
	}

	// ── LowCardinalityKeyNames ────────────────────────────────────────

	@Test
	void listenerId_keyName() {
		assertThat(ZaloBotListenerObservation.LowCardinalityKeyNames.LISTENER_ID.asString())
				.isEqualTo("zalobot.listener.id");
	}

	@Test
	void eventName_keyName() {
		assertThat(ZaloBotListenerObservation.LowCardinalityKeyNames.EVENT_NAME.asString())
				.isEqualTo("zalobot.event.name");
	}

	@Test
	void exception_keyName() {
		assertThat(ZaloBotListenerObservation.LowCardinalityKeyNames.EXCEPTION.asString())
				.isEqualTo("exception");
	}

	// ── DefaultZaloBotListenerObservationConvention ────────────────────

	@Test
	void defaultConvention_contextualName_includesEventName() {
		GetUpdatesResult update = new GetUpdatesResult(null, "message.text.received");
		ZaloBotListenerContext context = new ZaloBotListenerContext("default", update);

		ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention convention =
				ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention.INSTANCE;

		assertThat(convention.getContextualName(context)).isEqualTo("message.text.received process");
	}

	@Test
	void defaultConvention_lowCardinalityKeyValues_withoutError() {
		GetUpdatesResult update = new GetUpdatesResult(null, "message.text.received");
		ZaloBotListenerContext context = new ZaloBotListenerContext("my-listener", update);

		ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention convention =
				ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention.INSTANCE;

		KeyValues keyValues = convention.getLowCardinalityKeyValues(context);
		assertThat(keyValues).contains(
				KeyValue.of("zalobot.listener.id", "my-listener"),
				KeyValue.of("zalobot.event.name", "message.text.received"),
				KeyValue.of("exception", KeyValue.NONE_VALUE));
	}

	@Test
	void defaultConvention_lowCardinalityKeyValues_withError() {
		GetUpdatesResult update = new GetUpdatesResult(null, "message.text.received");
		ZaloBotListenerContext context = new ZaloBotListenerContext("my-listener", update);
		context.setError(new RuntimeException("test error"));

		ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention convention =
				ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention.INSTANCE;

		KeyValues keyValues = convention.getLowCardinalityKeyValues(context);
		assertThat(keyValues).contains(KeyValue.of("exception", "RuntimeException"));
	}

	// ── ZaloBotListenerObservationConvention interface ─────────────────

	@Test
	void convention_supportsContext_returnsTrueForListenerContext() {
		ZaloBotListenerObservationConvention convention =
				ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention.INSTANCE;

		GetUpdatesResult update = new GetUpdatesResult(null, "event");
		ZaloBotListenerContext listenerContext = new ZaloBotListenerContext("id", update);

		assertThat(convention.supportsContext(listenerContext)).isTrue();
	}

	@Test
	void convention_supportsContext_returnsFalseForOtherContext() {
		ZaloBotListenerObservationConvention convention =
				ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention.INSTANCE;

		Observation.Context otherContext = new Observation.Context();
		assertThat(convention.supportsContext(otherContext)).isFalse();
	}

	@Test
	void convention_getName_returnsExpected() {
		ZaloBotListenerObservationConvention convention =
				ZaloBotListenerObservation.DefaultZaloBotListenerObservationConvention.INSTANCE;

		assertThat(convention.getName()).isEqualTo("zalobot.listener");
	}
}
