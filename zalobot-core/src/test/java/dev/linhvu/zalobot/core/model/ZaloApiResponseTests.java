package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class ZaloApiResponseTests {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void recordAccessors_returnCorrectValues() {
		ZaloApiResponse<String> response = new ZaloApiResponse<>(true, "hello", 0);
		assertThat(response.ok()).isTrue();
		assertThat(response.result()).isEqualTo("hello");
		assertThat(response.errorCode()).isZero();
	}

	@Test
	void serializesToJson_withErrorCodeSnakeCase() throws Exception {
		ZaloApiResponse<String> response = new ZaloApiResponse<>(true, "hello", 0);
		String json = jsonMapper.writeValueAsString(response);
		assertThat(json).contains("\"error_code\"");
		assertThat(json).doesNotContain("\"errorCode\"");
	}

	@Test
	void deserializesFromJson_withErrorCodeSnakeCase() throws Exception {
		String json = """
				{"ok":true,"result":"hello","error_code":0}""";
		JavaType type = jsonMapper.getTypeFactory()
				.constructParametricType(ZaloApiResponse.class, String.class);
		ZaloApiResponse<String> response = jsonMapper.readValue(json, type);
		assertThat(response.ok()).isTrue();
		assertThat(response.result()).isEqualTo("hello");
		assertThat(response.errorCode()).isZero();
	}

	@Test
	void roundTrip_withNullResult() throws Exception {
		ZaloApiResponse<String> original = new ZaloApiResponse<>(false, null, 401);
		String json = jsonMapper.writeValueAsString(original);
		JavaType type = jsonMapper.getTypeFactory()
				.constructParametricType(ZaloApiResponse.class, String.class);
		ZaloApiResponse<String> deserialized = jsonMapper.readValue(json, type);
		assertThat(deserialized.ok()).isFalse();
		assertThat(deserialized.result()).isNull();
		assertThat(deserialized.errorCode()).isEqualTo(401);
	}

	@Test
	void roundTrip_withNestedObjectResult() throws Exception {
		GetMeResult meResult = new GetMeResult("123", "TestBot", "official", true);
		ZaloApiResponse<GetMeResult> original = new ZaloApiResponse<>(true, meResult, 0);
		String json = jsonMapper.writeValueAsString(original);
		JavaType type = jsonMapper.getTypeFactory()
				.constructParametricType(ZaloApiResponse.class, GetMeResult.class);
		ZaloApiResponse<GetMeResult> deserialized = jsonMapper.readValue(json, type);
		assertThat(deserialized.ok()).isTrue();
		assertThat(deserialized.result().id()).isEqualTo("123");
		assertThat(deserialized.result().accountName()).isEqualTo("TestBot");
	}

	@Test
	void equalsAndHashCode_forRecords() {
		ZaloApiResponse<String> a = new ZaloApiResponse<>(true, "hello", 0);
		ZaloApiResponse<String> b = new ZaloApiResponse<>(true, "hello", 0);
		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}
}
