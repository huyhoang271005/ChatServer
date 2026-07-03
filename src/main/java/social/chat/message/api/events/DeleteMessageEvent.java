package social.chat.message.api.events;

public record DeleteMessageEvent(
        Long conversationId
) {
}
