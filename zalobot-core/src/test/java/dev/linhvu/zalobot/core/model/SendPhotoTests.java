package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

class SendPhotoTests {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void validConstruction_succeeds() {
		SendPhoto photo = new SendPhoto("123", "caption", "https://example.com/photo.jpg");
		assertThat(photo.chatId()).isEqualTo("123");
		assertThat(photo.caption()).isEqualTo("caption");
		assertThat(photo.photo()).isEqualTo("https://example.com/photo.jpg");
	}

	@Test
	void nullChatId_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendPhoto(null, "caption", "photo.jpg"))
				.withMessage("chat_id cannot be null or empty");
	}

	@Test
	void emptyChatId_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendPhoto("", "caption", "photo.jpg"))
				.withMessage("chat_id cannot be null or empty");
	}

	@Test
	void nullPhoto_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendPhoto("123", "caption", null))
				.withMessage("photo cannot be null or empty");
	}

	@Test
	void emptyPhoto_throwsIllegalArgumentException() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new SendPhoto("123", "caption", ""))
				.withMessage("photo cannot be null or empty");
	}

	@Test
	void nullCaption_isAllowed() {
		SendPhoto photo = new SendPhoto("123", null, "photo.jpg");
		assertThat(photo.caption()).isNull();
	}

	@Test
	void serializesToJson_withSnakeCaseProperties() throws Exception {
		SendPhoto photo = new SendPhoto("123", "caption", "photo.jpg");
		String json = jsonMapper.writeValueAsString(photo);
		assertThat(json).contains("\"chat_id\"");
		assertThat(json).contains("\"caption\"");
		assertThat(json).contains("\"photo\"");
	}

	@Test
	void roundTrip_preservesAllFields() throws Exception {
		SendPhoto original = new SendPhoto("123", null, "photo.jpg");
		String json = jsonMapper.writeValueAsString(original);
		SendPhoto deserialized = jsonMapper.readValue(json, SendPhoto.class);
		assertThat(deserialized.chatId()).isEqualTo("123");
		assertThat(deserialized.caption()).isNull();
		assertThat(deserialized.photo()).isEqualTo("photo.jpg");
	}
}
