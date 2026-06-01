package social.chat.authorization.internal.cronjob;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "cron-config.authorization")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AuthorizationCronjobProperties {
    String cleanupRoleCron;
    Integer daysToKeepDeletedRole;
}
