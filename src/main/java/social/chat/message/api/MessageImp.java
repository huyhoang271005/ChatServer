package social.chat.message.api;

import org.springframework.modulith.NamedInterface;
import social.chat.message.api.dto.MessageDto;

@NamedInterface
public interface MessageImp {
    void saveMessage(MessageDto messageDto);
}
