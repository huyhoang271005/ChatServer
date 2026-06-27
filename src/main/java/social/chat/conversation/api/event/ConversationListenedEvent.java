package social.chat.conversation.api.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.conversation.api.ConversationImp;

@NamedInterface
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationListenedEvent {
    ConversationImp conversationImp;

    @ApplicationModuleListener
    public void saveConversation(RegisterSaveConversationEvent event){
        conversationImp.saveBatchPendingConversations();
    }
}
