package social.chat.message.internal;

import org.mapstruct.Mapper;
import social.chat.message.api.dto.MessageDto;

@Mapper(componentModel = "spring")
public interface MessageMapper {
    MessageDto toMessageDto(Message message);
    Message toMessage(MessageDto messageDto);
}
