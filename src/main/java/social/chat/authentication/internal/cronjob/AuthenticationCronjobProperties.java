package social.chat.authentication.internal.cronjob;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cron-config.authentication")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthenticationCronjobProperties {
    String cleanupDeviceCron;
    String revokedSessionExpiredCron;
    String cleanupSessionCron;
    Integer daysToKeepSessionExpired;
}
