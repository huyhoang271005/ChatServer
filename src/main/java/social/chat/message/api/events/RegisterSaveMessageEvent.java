package social.chat.message.api.events;

import social.chat.message.api.dto.MessageDto;

public record RegisterSaveMessageEvent(
        String title,
        MessageDto messageDto
) {
}
