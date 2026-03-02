package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.*;

class SendMessageResultTests {

	private final JsonMapper mapper = JsonMapper.builder().build();

	@Test
	void constructor_setsFields() {
		SendMessageResult result = new SendMessageResult("msg1", 1700000000L);
		assertThat(result.messageId()).isEqualTo("msg1");
		assertThat(result.date()).isEqualTo(1700000000L);
	}

	@Test
	void deserialization_readsJsonWithSnakeCaseFields() throws Exception {
		String json = """
				{"message_id":"msg123","date":1700000000}""";
		SendMessageResult result = mapper.readValue(json, SendMessageResult.class);
		assertThat(result.messageId()).isEqualTo("msg123");
		assertThat(result.date()).isEqualTo(1700000000L);
	}

	@Test
	void equalsAndHashCode() {
		SendMessageResult a = new SendMessageResult("msg1", 100L);
		SendMessageResult b = new SendMessageResult("msg1", 100L);
		assertThat(a).isEqualTo(b);
		assertThat(a.hashCode()).isEqualTo(b.hashCode());
	}

	@Test
	void notEqual_differentMessageId() {
		SendMessageResult a = new SendMessageResult("msg1", 100L);
		SendMessageResult b = new SendMessageResult("msg2", 100L);
		assertThat(a).isNotEqualTo(b);
	}
}
