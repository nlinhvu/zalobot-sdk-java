# ZaloBot SDK for Java

Java SDK for the [Zalo Bot API](https://bot-api.zaloplatforms.com) — client, listener, and Spring Boot integration.

## Requirements

- Java 21 or later
- Maven 3.6+ (or use the included Maven Wrapper)

## Modules

| Module | Description |
|--------|-------------|
| `zalobot-core` | Data models (requests, responses, records) |
| `zalobot-client` | HTTP client with fluent API (OkHttp3 / JDK HttpClient) |
| `zalobot-listener` | Long-polling listener with producer-consumer architecture |
| `zalobot-spring-boot` | Spring Boot auto-configuration |
| `zalobot-spring-boot-starter` | Spring Boot starter (pulls in all dependencies) |

## Installation

### Maven

**Spring Boot Starter** (recommended for Spring Boot apps):

```xml
<dependency>
    <groupId>dev.linhvu</groupId>
    <artifactId>zalobot-spring-boot-starter</artifactId>
    <version>0.0.1</version>
</dependency>
```

**Standalone Client** (without Spring Boot):

```xml
<dependency>
    <groupId>dev.linhvu</groupId>
    <artifactId>zalobot-client</artifactId>
    <version>0.0.1</version>
</dependency>
```

**With Long-Polling Listener** (without Spring Boot):

```xml
<dependency>
    <groupId>dev.linhvu</groupId>
    <artifactId>zalobot-listener</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Optional: OkHttp3

By default, the SDK uses the JDK HttpClient. To use OkHttp3 instead, add:

```xml
<dependency>
    <groupId>com.squareup.okhttp3</groupId>
    <artifactId>okhttp</artifactId>
    <version>4.12.0</version>
</dependency>
```

The SDK auto-detects OkHttp3 on the classpath and uses it when available.

## Quick Start

### Spring Boot

1. Add the starter dependency (see above).

2. Set your bot token in `application.yml`:

```yaml
zalobot:
  bot-token: ${ZALO_BOT_TOKEN}
  listener:
    poll-timeout: 30s
```

3. Create a listener component:

```java
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
```

4. Run your Spring Boot application. The listener starts polling automatically.

### Standalone (without Spring Boot)

```java
ZaloBotClient client = ZaloBotClient.botToken("your-bot-token");

// Get bot info
ZaloApiResponse<GetMeResult> me = client.getMe()
        .retrieve()
        .call(GetMeResult.class);

// Send a text message
client.sendMessage()
        .body(new SendMessage("chat-id", "Hello!"))
        .retrieve()
        .call(SendMessageResult.class);

// Send a photo
client.sendPhoto()
        .body(new SendPhoto("chat-id", "https://example.com/photo.jpg", "caption"))
        .retrieve()
        .call(SendMessageResult.class);

// Send a sticker
client.sendSticker()
        .body(new SendSticker("chat-id", "sticker-id"))
        .retrieve()
        .call(SendMessageResult.class);

// Send typing indicator
client.sendChatAction()
        .body(SendChatAction.typing("chat-id"))
        .retrieve()
        .call(SendMessageResult.class);
```

### Advanced Client Configuration

```java
ZaloBotClient client = ZaloBotClient.builder()
        .botToken("your-bot-token")
        .zaloBotUrl(new ZaloBotUrl("https", "bot-api.zaloplatforms.com", 443))
        .requestFactory(customFactory)  // custom HTTP client
        .jsonMapper(customMapper)       // custom Jackson mapper
        .build();
```

## Configuration Reference

All properties are under the `zalobot.` prefix:

| Property | Default | Description |
|----------|---------|-------------|
| `bot-token` | — | **Required.** Your Zalo bot token |
| `client.scheme` | `https` | API URL scheme |
| `client.host` | `bot-api.zaloplatforms.com` | API host |
| `client.port` | `443` | API port |
| `listener.enabled` | `true` | Enable/disable the listener |
| `listener.poll-timeout` | `30s` | Long-polling timeout |
| `listener.shutdown-timeout` | `10s` | Graceful shutdown timeout |
| `listener.back-off-interval` | `1s` | Initial backoff on errors |
| `listener.max-back-off-interval` | `30s` | Maximum backoff on errors |
| `listener.queue-capacity` | `64` | Internal queue size between poller and processors |
| `listener.processing-concurrency` | `1` | Number of processing threads |

## Customization (Spring Boot)

### Client Customizer

Implement `ZaloBotClientCustomizer` to customize the client builder before it is built:

```java
@Component
public class MyClientCustomizer implements ZaloBotClientCustomizer {
    @Override
    public void customize(ZaloBotClient.Builder builder) {
        builder.requestFactory(myCustomFactory);
    }
}
```

### Error Handler

Provide a custom `ErrorHandler` bean to handle listener errors:

```java
@Component
public class MyErrorHandler implements ErrorHandler {
    @Override
    public void handleError(Throwable t) {
        // custom error handling
    }
}
```

## Building from Source

```bash
./mvnw clean install
```

To skip tests:

```bash
./mvnw clean install -DskipTests
```

## Samples

See the [`zalobot-samples`](zalobot-samples/) directory for complete examples, including a Spring Boot echo bot.

## License

This project is licensed under the [MIT License](LICENSE).
