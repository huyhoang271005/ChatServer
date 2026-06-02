package social.chat.authentication.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import social.chat.authentication.api.events.AuthenticationCleanupDeviceRegisteredEvent;
import social.chat.authentication.api.events.AuthenticationCleanupSessionRegisteredEvent;
import social.chat.authentication.api.events.AuthenticationRevokedSessionRegisteredEvent;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationCronjob {
    ApplicationEventPublisher applicationEventPublisher;

    @Scheduled(cron = "#{@authenticationCronjobProperties.revokedSessionExpiredCron}")
    public void revokedSessionExpired() {
        applicationEventPublisher.publishEvent(new AuthenticationRevokedSessionRegisteredEvent());
    }

    @Scheduled(cron = "#{@authenticationCronjobProperties.cleanupDeviceCron}")
    public void cleanupDeviceCron() {
        applicationEventPublisher.publishEvent(new AuthenticationCleanupDeviceRegisteredEvent());
    }

    @Scheduled(cron = "#{@authenticationCronjobProperties.cleanupSessionCron}")
    public void cleanupSessionCron() {
        applicationEventPublisher.publishEvent(new AuthenticationCleanupSessionRegisteredEvent());
    }
}
