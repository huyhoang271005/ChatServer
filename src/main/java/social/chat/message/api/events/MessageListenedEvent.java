package social.chat.message.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.conversation.api.ConversationImp;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.message.api.MessageImp;
import social.chat.message.api.dto.MessageDto;

import java.util.List;

@NamedInterface
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageListenedEvent {
    ConversationImp conversationImp;
    MessageImp messageImp;

    @ApplicationModuleListener
    public void saveMessage(RegisterSaveMessageEvent event){
        MessageDto messageDto = event.messageDto();
        messageImp.saveMessage(messageDto);
        ConversationDto conversationDto = conversationImp.getConversations(List.of(messageDto
                .getConversationId())).getFirst();
        conversationImp.putConversation(conversationDto, true);
    }
}
