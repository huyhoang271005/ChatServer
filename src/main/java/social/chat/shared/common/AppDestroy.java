package social.chat.shared.common;

import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import social.chat.conversation.api.ConversationImp;
import social.chat.message.api.MessageImp;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppDestroy {
    ConversationImp conversationImp;
    MessageImp messageImp;

    @PreDestroy
    public void destroy(){
        conversationImp.saveAllPendingConversations();
        messageImp.saveAllPendingMessages();
        messageImp.saveAllPendingReactor();
    }
}
