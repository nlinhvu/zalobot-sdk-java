package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class SendChatActionTests {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void typing_createsCorrectAction() {
		SendChatAction action = SendChatAction.typing("123");
		assertThat(action.chatId()).isEqualTo("123");
		assertThat(action.action()).isEqualTo("typing");
	}

	@Test
	void uploadPhoto_createsCorrectAction() {
		SendChatAction action = SendChatAction.uploadPhoto("123");
		assertThat(action.chatId()).isEqualTo("123");
		assertThat(action.action()).isEqualTo("upload_photo");
	}

	@Test
	void serializesToJson_withSnakeCaseProperties() throws Exception {
		SendChatAction action = SendChatAction.typing("123");
		String json = jsonMapper.writeValueAsString(action);
		assertThat(json).contains("\"chat_id\"");
		assertThat(json).contains("\"action\"");
	}

	@Test
	void roundTrip_preservesAllFields() throws Exception {
		SendChatAction original = SendChatAction.uploadPhoto("456");
		String json = jsonMapper.writeValueAsString(original);
		SendChatAction deserialized = jsonMapper.readValue(json, SendChatAction.class);
		assertThat(deserialized).isEqualTo(original);
	}
}
