package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class GetMeResultTests {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void recordAccessors_returnCorrectValues() {
		GetMeResult result = new GetMeResult("123", "TestBot", "official", true);
		assertThat(result.id()).isEqualTo("123");
		assertThat(result.accountName()).isEqualTo("TestBot");
		assertThat(result.accountType()).isEqualTo("official");
		assertThat(result.canJoinGroups()).isTrue();
	}

	@Test
	void serializesToJson_withSnakeCaseProperties() throws Exception {
		GetMeResult result = new GetMeResult("123", "TestBot", "official", true);
		String json = jsonMapper.writeValueAsString(result);
		assertThat(json).contains("\"account_name\"");
		assertThat(json).contains("\"account_type\"");
		assertThat(json).contains("\"can_join_groups\"");
		assertThat(json).doesNotContain("\"accountName\"");
	}

	@Test
	void deserializesFromJson_withSnakeCaseProperties() throws Exception {
		String json = """
				{"id":"123","account_name":"TestBot","account_type":"official","can_join_groups":true}""";
		GetMeResult result = jsonMapper.readValue(json, GetMeResult.class);
		assertThat(result.id()).isEqualTo("123");
		assertThat(result.accountName()).isEqualTo("TestBot");
		assertThat(result.accountType()).isEqualTo("official");
		assertThat(result.canJoinGroups()).isTrue();
	}

	@Test
	void roundTrip_preservesAllFields() throws Exception {
		GetMeResult original = new GetMeResult("456", "MyBot", "trial", false);
		String json = jsonMapper.writeValueAsString(original);
		GetMeResult deserialized = jsonMapper.readValue(json, GetMeResult.class);
		assertThat(deserialized).isEqualTo(original);
	}
}
