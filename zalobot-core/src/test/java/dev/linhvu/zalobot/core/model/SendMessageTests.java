package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SendMessageTests {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void validConstruction_succeeds() {
		SendMessage msg = new SendMessage("123", "hello");
		assertThat(msg.chat_id()).isEqualTo("123");
		assertThat(msg.text()).isEqualTo("hello");
	}

	@Test
	void nullChatId_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendMessage(null, "hello"))
				.withMessage("chat_id cannot be null or empty");
	}

	@Test
	void emptyChatId_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendMessage("", "hello"))
				.withMessage("chat_id cannot be null or empty");
	}

	@Test
	void nullText_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendMessage("123", null))
				.withMessage("text cannot be null or empty");
	}

	@Test
	void emptyText_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendMessage("123", ""))
				.withMessage("text cannot be null or empty");
	}

	@Test
	void textExceeds2000Chars_throwsIllegalArgumentException() {
		String longText = "a".repeat(2001);
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendMessage("123", longText))
				.withMessage("text cannot exceed 2000 characters");
	}

	@Test
	void textExactly2000Chars_succeeds() {
		String text2000 = "a".repeat(2000);
		SendMessage msg = new SendMessage("123", text2000);
		assertThat(msg.text()).hasSize(2000);
	}

	@Test
	void serializesToJson_withSnakeCaseChatId() throws Exception {
		SendMessage msg = new SendMessage("123", "hello");
		String json = jsonMapper.writeValueAsString(msg);
		assertThat(json).contains("\"chat_id\"");
		assertThat(json).contains("\"text\"");
	}

	@Test
	void deserializesFromJson() throws Exception {
		String json = """
				{"chat_id":"123","text":"hello"}""";
		SendMessage msg = jsonMapper.readValue(json, SendMessage.class);
		assertThat(msg.chat_id()).isEqualTo("123");
		assertThat(msg.text()).isEqualTo("hello");
	}
}
