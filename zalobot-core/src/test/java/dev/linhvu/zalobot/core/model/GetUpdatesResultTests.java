package dev.linhvu.zalobot.core.model;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class GetUpdatesResultTests {

	private final JsonMapper jsonMapper = JsonMapper.builder().build();

	@Test
	void isTextMessage_returnsTrueForTextEvent() {
		GetUpdatesResult result = new GetUpdatesResult(null, "message.text.received");
		assertThat(result.isTextMessage()).isTrue();
	}

	@Test
	void isTextMessage_returnsFalseForOtherEvents() {
		GetUpdatesResult result = new GetUpdatesResult(null, "message.image.received");
		assertThat(result.isTextMessage()).isFalse();
	}

	@Test
	void isImageMessage_returnsTrueForImageEvent() {
		GetUpdatesResult result = new GetUpdatesResult(null, "message.image.received");
		assertThat(result.isImageMessage()).isTrue();
	}

	@Test
	void isImageMessage_returnsFalseForOtherEvents() {
		GetUpdatesResult result = new GetUpdatesResult(null, "message.text.received");
		assertThat(result.isImageMessage()).isFalse();
	}

	@Test
	void isStickerMessage_returnsTrueForStickerEvent() {
		GetUpdatesResult result = new GetUpdatesResult(null, "message.sticker.received");
		assertThat(result.isStickerMessage()).isTrue();
	}

	@Test
	void isStickerMessage_returnsFalseForOtherEvents() {
		GetUpdatesResult result = new GetUpdatesResult(null, "message.text.received");
		assertThat(result.isStickerMessage()).isFalse();
	}

	@Test
	void allChecks_returnFalseForNullEventName() {
		GetUpdatesResult result = new GetUpdatesResult(null, null);
		assertThat(result.isTextMessage()).isFalse();
		assertThat(result.isImageMessage()).isFalse();
		assertThat(result.isStickerMessage()).isFalse();
	}

	@Test
	void nestedRecords_serializationRoundTrip() throws Exception {
		GetUpdatesResult.From from = new GetUpdatesResult.From("user1", false, "John");
		GetUpdatesResult.Chat chat = new GetUpdatesResult.Chat("chat1", "private");
		GetUpdatesResult.Message message = new GetUpdatesResult.Message(chat, "Hello", "msg1", 1234567890L, from);
		GetUpdatesResult original = new GetUpdatesResult(message, "message.text.received");

		String json = jsonMapper.writeValueAsString(original);
		GetUpdatesResult deserialized = jsonMapper.readValue(json, GetUpdatesResult.class);

		assertThat(deserialized.eventName()).isEqualTo("message.text.received");
		assertThat(deserialized.message().text()).isEqualTo("Hello");
		assertThat(deserialized.message().chat().id()).isEqualTo("chat1");
		assertThat(deserialized.message().from().displayName()).isEqualTo("John");
	}

	@Test
	void deserializesFromJson_fullPayload() throws Exception {
		String json = """
				{
				  "message": {
				    "chat": {"id": "chat1", "chat_type": "private"},
				    "text": "Hello world",
				    "message_id": "msg123",
				    "date": 1700000000,
				    "from": {"id": "user1", "is_bot": false, "display_name": "Alice"}
				  },
				  "event_name": "message.text.received"
				}""";
		GetUpdatesResult result = jsonMapper.readValue(json, GetUpdatesResult.class);
		assertThat(result.isTextMessage()).isTrue();
		assertThat(result.message().text()).isEqualTo("Hello world");
		assertThat(result.message().messageId()).isEqualTo("msg123");
		assertThat(result.message().chat().chatType()).isEqualTo("private");
		assertThat(result.message().from().isBot()).isFalse();
		assertThat(result.message().from().displayName()).isEqualTo("Alice");
	}
}
