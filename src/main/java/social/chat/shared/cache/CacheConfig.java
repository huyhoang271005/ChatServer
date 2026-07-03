package social.chat.shared.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import social.chat.authentication.api.JwtProperties;
import social.chat.shared.common.GlobalParamName;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CacheConfig {
    JwtProperties jwtProperties;
    CacheExpireWriteAccessProperties cacheExpireWriteAccessProperties;

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        CaffeineCache sessionCache = new CaffeineCache(
                GlobalParamName.CacheName.SESSION,
                Caffeine.newBuilder()
                        .expireAfterAccess(jwtProperties.getAccessTokenExpire(),
                                TimeUnit.SECONDS)
                        .maximumSize(1000)
                        .build()
        );


        CaffeineCache userCache = new CaffeineCache(
                GlobalParamName.CacheName.USER,
                Caffeine.newBuilder()
                        .expireAfterAccess(10, TimeUnit.MINUTES)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .maximumSize(1000)
                        .build()
        );

        CaffeineCache roleCache = new CaffeineCache(
                GlobalParamName.CacheName.ROLE,
                Caffeine.newBuilder()
                        .expireAfterAccess(15, TimeUnit.MINUTES)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .maximumSize(50)
                        .build()
        );

        CaffeineCache userFcmCache = new CaffeineCache(
                GlobalParamName.CacheName.USER_FCM,
                Caffeine.newBuilder()
                        .expireAfterAccess(5, TimeUnit.MINUTES)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .maximumSize(1000)
                        .build()
        );

        CaffeineCache userShortProfile = new CaffeineCache(
                GlobalParamName.CacheName.USER_SHORT_PROFILE,
                Caffeine.newBuilder()
                        .expireAfterAccess(10, TimeUnit.MINUTES)
                        .expireAfterWrite(24, TimeUnit.HOURS)
                        .maximumSize(500)
                        .build()
        );

        CaffeineCache conversationCache = new CaffeineCache(
                GlobalParamName.CacheName.CONVERSATION,
                Caffeine.newBuilder()
                        .expireAfterAccess(cacheExpireWriteAccessProperties.getConversation())
                        .maximumSize(10000)
                        .build()
        );

        CaffeineCache messageCache = new CaffeineCache(
                GlobalParamName.CacheName.MESSAGE,
                Caffeine.newBuilder()
                        .expireAfterAccess(cacheExpireWriteAccessProperties.getMessage())
                        .maximumSize(10000)
                        .build()
        );

        CaffeineCache reactorCache = new CaffeineCache(
                GlobalParamName.CacheName.REACTION,
                Caffeine.newBuilder()
                        .expireAfterAccess(cacheExpireWriteAccessProperties.getReactor())
                        .maximumSize(10000)
                        .build()
        );

        CaffeineCache userPresenceCache = new CaffeineCache(
                GlobalParamName.CacheName.USER_PRESENCE,
                Caffeine.newBuilder()
                        .expireAfterAccess(cacheExpireWriteAccessProperties.getUserPresence())
                        .maximumSize(1000)
                        .build()
        );

        cacheManager.setCaches(List.of(sessionCache, userCache, roleCache, userShortProfile,
                userFcmCache, conversationCache, messageCache, reactorCache, userPresenceCache));
        return cacheManager;
    }
}
