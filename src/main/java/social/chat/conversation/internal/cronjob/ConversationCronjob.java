package social.chat.conversation.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import social.chat.conversation.api.event.RegisterSaveConversationEvent;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationCronjob {
    ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(fixedDelayString = "#{@cacheExpireWriteAccessProperties.conversation.toMillis() / 2}")
    public void saveConversationCron(){
        applicationEventPublisher.publishEvent(new RegisterSaveConversationEvent());
    }
}
