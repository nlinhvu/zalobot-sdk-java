package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SendStickerTests {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void validConstruction_succeeds() {
		SendSticker sticker = new SendSticker("123", "sticker-id");
		assertThat(sticker.chatId()).isEqualTo("123");
		assertThat(sticker.sticker()).isEqualTo("sticker-id");
	}

	@Test
	void nullChatId_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendSticker(null, "sticker-id"))
				.withMessage("chat_id cannot be null or empty");
	}

	@Test
	void emptyChatId_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendSticker("", "sticker-id"))
				.withMessage("chat_id cannot be null or empty");
	}

	@Test
	void nullSticker_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendSticker("123", null))
				.withMessage("sticker cannot be null or empty");
	}

	@Test
	void emptySticker_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendSticker("123", ""))
				.withMessage("sticker cannot be null or empty");
	}

	@Test
	void serializesToJson() throws Exception {
		SendSticker sticker = new SendSticker("123", "sticker-id");
		String json = jsonMapper.writeValueAsString(sticker);
		assertThat(json).contains("\"chat_id\"");
		assertThat(json).contains("\"sticker\"");
	}

	@Test
	void roundTrip_preservesAllFields() throws Exception {
		SendSticker original = new SendSticker("123", "sticker-id");
		String json = jsonMapper.writeValueAsString(original);
		SendSticker deserialized = jsonMapper.readValue(json, SendSticker.class);
		assertThat(deserialized).isEqualTo(original);
	}
}
