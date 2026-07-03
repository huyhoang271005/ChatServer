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
    public void saveBatchMessage(SaveBatchMessageEvent event){
        messageImp.saveBatchPendingMessage();
    }

    @ApplicationModuleListener
    public void saveBatchReactor(SaveBatchReactorEvent event){
        messageImp.saveBatchPendingReactor();
    }

    @ApplicationModuleListener
    public void deleteMessage(DeleteMessageEvent event){
        messageImp.deleteByConversationId(event.conversationId());
    }

    @ApplicationModuleListener
    public void saveAllMessage(SaveAllMessageEvent event){
        messageImp.saveAllPendingMessages();
    }

    @ApplicationModuleListener
    public void saveAllReactor(SaveBatchReactorEvent event){
        messageImp.saveAllPendingReactor();
    }
}
