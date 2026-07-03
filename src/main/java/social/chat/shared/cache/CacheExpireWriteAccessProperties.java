package social.chat.shared.cache;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@ConfigurationProperties(prefix = "cache.expire-after-access")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CacheExpireWriteAccessProperties {
    Duration message;
    Duration conversation;
    Duration reactor;
    Duration userPresence;
}
