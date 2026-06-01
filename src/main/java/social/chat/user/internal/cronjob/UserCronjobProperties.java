package social.chat.user.internal.cronjob;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cron-config.user")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserCronjobProperties {
    String cleanupUserCron;
    Integer daysToKeepDeletedUser;
    String unbannedUserCron;
}
