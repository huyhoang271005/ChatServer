package social.chat.message.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.message.api.events.SaveBatchMessageEvent;
import social.chat.message.api.events.SaveBatchReactorEvent;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageCronjob {
    ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(fixedDelayString = "#{@cacheExpireWriteAccessProperties.message.toMillis() / 2}")
    @Transactional
    public void saveMessageCron(){
        applicationEventPublisher.publishEvent(new SaveBatchMessageEvent());
    }

    @Scheduled(fixedDelayString = "#{@cacheExpireWriteAccessProperties.reactor.toMillis() / 2}")
    public void saveReactorCron(){
        applicationEventPublisher.publishEvent(new SaveBatchReactorEvent());
    }
}
