package social.chat.message.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.message.api.MessageImp;

@NamedInterface
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageListenedEvent {
    MessageImp messageImp;

    @ApplicationModuleListener
    public void saveMessage(RegisterSaveMessageEvent event){
        messageImp.saveBatchPendingMessage();
    }

    @ApplicationModuleListener
    public void saveReactor(RegisterSaveReactorEvent event){
        messageImp.saveBatchPendingReactor();
    }

    @ApplicationModuleListener
    public void deleteMessage(RegisterDeleteMessageEvent event){
        messageImp.deleteByConversationId(event.conversationId());
    }
}
