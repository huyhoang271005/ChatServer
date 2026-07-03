package social.chat.shared.common;

import jakarta.annotation.PreDestroy;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import social.chat.conversation.api.event.SaveAllConversationEvent;
import social.chat.message.api.events.SaveAllMessageEvent;
import social.chat.message.api.events.SaveAllReactorEvent;
import social.chat.user_presence.api.event.SaveAllUserPresence;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AppDestroy {
    ApplicationEventPublisher applicationEventPublisher;

    @PreDestroy
    public void destroy(){
        applicationEventPublisher.publishEvent(new SaveAllConversationEvent());
        applicationEventPublisher.publishEvent(new SaveAllMessageEvent());
        applicationEventPublisher.publishEvent(new SaveAllReactorEvent());
        applicationEventPublisher.publishEvent(new SaveAllUserPresence());
    }
}
