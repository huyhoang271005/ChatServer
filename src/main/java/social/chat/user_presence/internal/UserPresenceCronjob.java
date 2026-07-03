package social.chat.user_presence.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import social.chat.shared.cache.CacheExpireWriteAccessProperties;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPresenceCronjob {

    @Scheduled(fixedDelayString = "#{@cacheExpireWriteAccessProperties.userPresence.toMillis() / 2}")
    public void saveUserPresenceCache(){

    }
}
