package social.chat.message.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.message.api.MessageImp;
import social.chat.message.api.dto.MessageDto;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageLogicService implements MessageImp {
    MessageRepository messageRepository;
    MessageMapper messageMapper;

    @Override
    @Transactional
    public void saveMessage(MessageDto messageDto) {
        Message message = messageMapper.toMessage(messageDto);
        messageRepository.save(message);
    }
}
