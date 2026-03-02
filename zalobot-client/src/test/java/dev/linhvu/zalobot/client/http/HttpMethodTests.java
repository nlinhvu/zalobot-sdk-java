package dev.linhvu.zalobot.client.http;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class HttpMethodTests {

	@Test
	void predefinedConstants_haveCorrectNames() {
		assertThat(HttpMethod.GET.name()).isEqualTo("GET");
		assertThat(HttpMethod.HEAD.name()).isEqualTo("HEAD");
		assertThat(HttpMethod.POST.name()).isEqualTo("POST");
		assertThat(HttpMethod.PUT.name()).isEqualTo("PUT");
		assertThat(HttpMethod.PATCH.name()).isEqualTo("PATCH");
		assertThat(HttpMethod.DELETE.name()).isEqualTo("DELETE");
		assertThat(HttpMethod.OPTIONS.name()).isEqualTo("OPTIONS");
		assertThat(HttpMethod.TRACE.name()).isEqualTo("TRACE");
	}

	@Test
	void valueOf_returnsSameInstanceForKnownMethods() {
		assertThat(HttpMethod.valueOf("GET")).isSameAs(HttpMethod.GET);
		assertThat(HttpMethod.valueOf("HEAD")).isSameAs(HttpMethod.HEAD);
		assertThat(HttpMethod.valueOf("POST")).isSameAs(HttpMethod.POST);
		assertThat(HttpMethod.valueOf("PUT")).isSameAs(HttpMethod.PUT);
		assertThat(HttpMethod.valueOf("PATCH")).isSameAs(HttpMethod.PATCH);
		assertThat(HttpMethod.valueOf("DELETE")).isSameAs(HttpMethod.DELETE);
		assertThat(HttpMethod.valueOf("OPTIONS")).isSameAs(HttpMethod.OPTIONS);
		assertThat(HttpMethod.valueOf("TRACE")).isSameAs(HttpMethod.TRACE);
	}

	@Test
	void valueOf_createsNewInstanceForCustomMethod() {
		HttpMethod custom = HttpMethod.valueOf("CUSTOM");
		assertThat(custom.name()).isEqualTo("CUSTOM");
		assertThat(custom).isNotSameAs(HttpMethod.GET);
	}

	@Test
	void matches_returnsTrueForSameName() {
		assertThat(HttpMethod.POST.matches("POST")).isTrue();
		assertThat(HttpMethod.GET.matches("GET")).isTrue();
	}

	@Test
	void matches_returnsFalseForDifferentName() {
		assertThat(HttpMethod.POST.matches("GET")).isFalse();
		assertThat(HttpMethod.GET.matches("POST")).isFalse();
	}
}