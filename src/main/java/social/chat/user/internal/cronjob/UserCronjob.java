package social.chat.user.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.user.api.events.UserHardDeleteUserRegisteredEvent;
import social.chat.user.api.events.UserUnbannedAccountRegisteredEvent;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserCronjob {
    ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "#{@userCronjobProperties.unbannedUserCron}")
    @Transactional
    public void updateAccountStatusBanned() {
        applicationEventPublisher.publishEvent(new UserUnbannedAccountRegisteredEvent());
    }

    @Scheduled(cron = "#{@userCronjobProperties.cleanupUserCron}")
    @Transactional
    public void hardDeleteUser() {
        applicationEventPublisher.publishEvent(new UserHardDeleteUserRegisteredEvent());
    }
}
