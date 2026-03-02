package dev.linhvu.zalobot.listener.observation;

import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class ZaloBotListenerContextTests {

	@Test
	void constructor_setsListenerIdAndUpdate() {
		GetUpdatesResult update = new GetUpdatesResult(null, "message.text.received");
		ZaloBotListenerContext context = new ZaloBotListenerContext("my-listener", update);

		assertThat(context.getListenerId()).isEqualTo("my-listener");
		assertThat(context.getUpdate()).isSameAs(update);
	}

	@Test
	void getEventName_returnsEventNameFromUpdate() {
		GetUpdatesResult update = new GetUpdatesResult(null, "message.photo.received");
		ZaloBotListenerContext context = new ZaloBotListenerContext("id", update);

		assertThat(context.getEventName()).isEqualTo("message.photo.received");
	}

	@Test
	void getEventName_returnsUnknownWhenNull() {
		GetUpdatesResult update = new GetUpdatesResult(null, null);
		ZaloBotListenerContext context = new ZaloBotListenerContext("id", update);

		assertThat(context.getEventName()).isEqualTo("unknown");
	}
}
