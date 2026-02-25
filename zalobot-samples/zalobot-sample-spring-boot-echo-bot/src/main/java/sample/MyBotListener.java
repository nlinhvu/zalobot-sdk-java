package sample;

import dev.linhvu.zalobot.client.ZaloBotClient;
import dev.linhvu.zalobot.core.model.GetUpdatesResult;
import dev.linhvu.zalobot.core.model.SendMessage;
import dev.linhvu.zalobot.core.model.SendMessageResult;
import dev.linhvu.zalobot.listener.UpdateListener;

import org.springframework.stereotype.Component;

@Component
public class MyBotListener implements UpdateListener {

	private final ZaloBotClient client;

	public MyBotListener(ZaloBotClient client) {
		this.client = client;
	}

	@Override
	public void onUpdate(GetUpdatesResult update) {
		if (update.isTextMessage()) {
			String chatId = update.message().chat().id();
			String text = update.message().text();

			client.sendMessage()
					.body(new SendMessage(chatId, "Echo: " + text))
					.retrieve()
					.call(SendMessageResult.class);
		}
	}
}
