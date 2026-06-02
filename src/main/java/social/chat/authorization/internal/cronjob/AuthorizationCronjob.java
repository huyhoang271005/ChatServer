package social.chat.authorization.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import social.chat.authorization.api.events.AuthorizationHardDeleteRegisteredEvent;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationCronjob {
    ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "#{@authorizationCronjobProperties.cleanupRoleCron}")
    public void hardDeleteRole() {
        applicationEventPublisher.publishEvent(new AuthorizationHardDeleteRegisteredEvent());
    }

}
