package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.*;

class GetUpdatesTests {

	private final JsonMapper mapper = JsonMapper.builder().build();

	@Test
	void constructor_setsTimeout() {
		GetUpdates getUpdates = new GetUpdates(30L);
		assertThat(getUpdates.timeout()).isEqualTo(30L);
	}

	@Test
	void constructor_nullTimeout() {
		GetUpdates getUpdates = new GetUpdates(null);
		assertThat(getUpdates.timeout()).isNull();
	}

	@Test
	void serialization_producesExpectedJson() throws Exception {
		GetUpdates getUpdates = new GetUpdates(60L);
		String json = mapper.writeValueAsString(getUpdates);
		assertThat(json).contains("\"timeout\":60");
	}

	@Test
	void deserialization_readsJson() throws Exception {
		String json = """
				{"timeout":30}""";
		GetUpdates getUpdates = mapper.readValue(json, GetUpdates.class);
		assertThat(getUpdates.timeout()).isEqualTo(30L);
	}

	@Test
	void equalsAndHashCode() {
		GetUpdates a = new GetUpdates(30L);
		GetUpdates b = new GetUpdates(30L);
		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}
}
