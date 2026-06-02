package social.chat.verification.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.verification.api.events.VerificationExpiredRegisteredEvent;
import social.chat.verification.api.events.VerificationHardDeleteRegisteredEvent;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationCronjob {
    ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "#{@verificationCronjobProperties.expiredVerificationCron}")
    @Transactional
    public void expiredVerification() {
        applicationEventPublisher.publishEvent(new VerificationExpiredRegisteredEvent());
    }

    @Scheduled(cron = "#{@verificationCronjobProperties.cleanupVerificationCron}")
    @Transactional
    public void hardDeleteVerification() {
        applicationEventPublisher.publishEvent(new VerificationHardDeleteRegisteredEvent());
    }
}
