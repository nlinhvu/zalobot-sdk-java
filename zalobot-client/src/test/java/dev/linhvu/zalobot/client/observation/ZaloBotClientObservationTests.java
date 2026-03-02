package dev.linhvu.zalobot.client.observation;

import java.net.URI;
import java.util.Map;

import dev.linhvu.zalobot.client.http.ClientHttpRequest;
import dev.linhvu.zalobot.client.http.HttpMethod;
import io.micrometer.common.KeyValue;
import io.micrometer.common.KeyValues;
import io.micrometer.observation.Observation;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class ZaloBotClientObservationTests {

	// ── ZaloBotClientContext ──────────────────────────────────────────

	@Test
	void context_storesMethodPathAndHttpMethod() {
		ZaloBotClientContext context = new ZaloBotClientContext("sendMessage", HttpMethod.POST);
		assertThat(context.getMethodPath()).isEqualTo("sendMessage");
		assertThat(context.getHttpMethod()).isEqualTo(HttpMethod.POST);
	}

	@Test
	void context_defaultSuccessIsTrue() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		assertThat(context.isSuccess()).isTrue();
	}

	@Test
	void context_setSuccessChangesValue() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		context.setSuccess(false);
		assertThat(context.isSuccess()).isFalse();
	}

	@Test
	void context_defaultExceptionNameIsNone() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		assertThat(context.getExceptionName()).isEqualTo(KeyValue.NONE_VALUE);
	}

	@Test
	void context_setExceptionNameChangesValue() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		context.setExceptionName("RuntimeException");
		assertThat(context.getExceptionName()).isEqualTo("RuntimeException");
	}

	@Test
	void context_getRequestUrl_withCarrier() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		ClientHttpRequest request = mock(ClientHttpRequest.class);
		given(request.getURI()).willReturn(URI.create("https://api.example.com/botTOKEN/getMe"));
		context.setCarrier(request);
		assertThat(context.getRequestUrl()).isEqualTo("https://api.example.com/botTOKEN/getMe");
	}

	@Test
	void context_getRequestUrl_withoutCarrier() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		assertThat(context.getRequestUrl()).isEqualTo("unknown");
	}

	@Test
	void context_remoteServiceName_isSet() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		assertThat(context.getRemoteServiceName()).isEqualTo("Zalo Bot API");
	}

	@Test
	void context_propagator_injectsHeaderIntoCarrier() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		ClientHttpRequest request = mock(ClientHttpRequest.class);
		Map<String, String> headers = new java.util.LinkedHashMap<>();
		given(request.getHeaders()).willReturn(headers);
		given(request.getURI()).willReturn(URI.create("https://example.com"));
		context.setCarrier(request);

		// Invoke the propagator setter
		context.getSetter().set(request, "X-Trace-Id", "abc123");
		assertThat(headers).containsEntry("X-Trace-Id", "abc123");
	}

	@Test
	void context_propagator_doesNotThrowWhenCarrierIsNull() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		// Should not throw when request is null
		context.getSetter().set(null, "X-Trace-Id", "abc123");
	}

	// ── ObservationDocumentation ──────────────────────────────────────

	@Test
	void apiRequest_defaultConventionClass() {
		assertThat(ZaloBotClientObservation.API_REQUEST.getDefaultConvention())
				.isEqualTo(ZaloBotClientObservation.DefaultZaloBotClientObservationConvention.class);
	}

	@Test
	void apiRequest_lowCardinalityKeyNames() {
		assertThat(ZaloBotClientObservation.API_REQUEST.getLowCardinalityKeyNames())
				.containsExactly(ZaloBotClientObservation.LowCardinalityKeyNames.values());
	}

	@Test
	void apiRequest_highCardinalityKeyNames() {
		assertThat(ZaloBotClientObservation.API_REQUEST.getHighCardinalityKeyNames())
				.containsExactly(ZaloBotClientObservation.HighCardinalityKeyNames.values());
	}

	// ── LowCardinalityKeyNames ────────────────────────────────────────

	@Test
	void method_keyName() {
		assertThat(ZaloBotClientObservation.LowCardinalityKeyNames.METHOD.asString())
				.isEqualTo("zalobot.method");
	}

	@Test
	void outcome_keyName() {
		assertThat(ZaloBotClientObservation.LowCardinalityKeyNames.OUTCOME.asString())
				.isEqualTo("outcome");
	}

	@Test
	void exception_keyName() {
		assertThat(ZaloBotClientObservation.LowCardinalityKeyNames.EXCEPTION.asString())
				.isEqualTo("exception");
	}

	// ── HighCardinalityKeyNames ───────────────────────────────────────

	@Test
	void requestUrl_keyName() {
		assertThat(ZaloBotClientObservation.HighCardinalityKeyNames.REQUEST_URL.asString())
				.isEqualTo("zalobot.request.url");
	}

	// ── DefaultZaloBotClientObservationConvention ──────────────────────

	@Test
	void defaultConvention_contextualName() {
		ZaloBotClientContext context = new ZaloBotClientContext("sendMessage", HttpMethod.POST);
		ZaloBotClientObservation.DefaultZaloBotClientObservationConvention convention =
				ZaloBotClientObservation.DefaultZaloBotClientObservationConvention.INSTANCE;

		assertThat(convention.getContextualName(context)).isEqualTo("sendMessage");
	}

	@Test
	void defaultConvention_lowCardinalityKeyValues_success() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);

		KeyValues keyValues = ZaloBotClientObservation.DefaultZaloBotClientObservationConvention.INSTANCE
				.getLowCardinalityKeyValues(context);

		assertThat(keyValues).contains(
				KeyValue.of("zalobot.method", "getMe"),
				KeyValue.of("outcome", "SUCCESS"),
				KeyValue.of("exception", KeyValue.NONE_VALUE));
	}

	@Test
	void defaultConvention_lowCardinalityKeyValues_error() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		context.setSuccess(false);
		context.setError(new RuntimeException("test"));

		KeyValues keyValues = ZaloBotClientObservation.DefaultZaloBotClientObservationConvention.INSTANCE
				.getLowCardinalityKeyValues(context);

		assertThat(keyValues).contains(
				KeyValue.of("outcome", "ERROR"),
				KeyValue.of("exception", "RuntimeException"));
	}

	@Test
	void defaultConvention_highCardinalityKeyValues() {
		ZaloBotClientContext context = new ZaloBotClientContext("getMe", HttpMethod.POST);
		ClientHttpRequest request = mock(ClientHttpRequest.class);
		given(request.getURI()).willReturn(URI.create("https://api.example.com/botTOKEN/getMe"));
		context.setCarrier(request);

		KeyValues keyValues = ZaloBotClientObservation.DefaultZaloBotClientObservationConvention.INSTANCE
				.getHighCardinalityKeyValues(context);

		assertThat(keyValues).contains(
				KeyValue.of("zalobot.request.url", "https://api.example.com/botTOKEN/getMe"));
	}

	// ── ZaloBotClientObservationConvention interface ───────────────────

	@Test
	void convention_supportsContext_returnsTrueForClientContext() {
		ZaloBotClientObservationConvention convention =
				ZaloBotClientObservation.DefaultZaloBotClientObservationConvention.INSTANCE;

		ZaloBotClientContext clientContext = new ZaloBotClientContext("getMe", HttpMethod.POST);
		assertThat(convention.supportsContext(clientContext)).isTrue();
	}

	@Test
	void convention_supportsContext_returnsFalseForOtherContext() {
		ZaloBotClientObservationConvention convention =
				ZaloBotClientObservation.DefaultZaloBotClientObservationConvention.INSTANCE;

		Observation.Context otherContext = new Observation.Context();
		assertThat(convention.supportsContext(otherContext)).isFalse();
	}

	@Test
	void convention_getName_returnsExpected() {
		ZaloBotClientObservationConvention convention =
				ZaloBotClientObservation.DefaultZaloBotClientObservationConvention.INSTANCE;

		assertThat(convention.getName()).isEqualTo("zalobot.client.requests");
	}
}
