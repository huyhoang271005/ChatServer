package social.chat.message.internal.cronjob;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties("cron-config.message")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageCronjobProperties {
    int batchSize;
}
