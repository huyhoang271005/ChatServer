package social.chat.authentication.internal.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;
import social.chat.authentication.api.FcmTokenImp;
import social.chat.authentication.api.dto.UserIdWithFcmToken;
import social.chat.authentication.internal.repository.TokenRepository;
import social.chat.shared.common.GlobalParamName;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CacheConfig(cacheNames = GlobalParamName.CacheName.USER_FCM)
public class FcmTokenCache implements FcmTokenImp {
    TokenRepository tokenRepository;
    CacheManager cacheManager;

    @Override
    public List<String> getFcmTokenByUserIds(List<Long> userIds) {
        Cache cache = cacheManager.getCache(GlobalParamName.CacheName.USER_FCM);
        Set<String> fcmTokens = new HashSet<>();
        if(userIds.isEmpty() || cache == null) return new ArrayList<>(fcmTokens);
        List<Long> userIdsMissing = new ArrayList<>();
        for (Long userId : userIds) {
            Cache.ValueWrapper objFcmToken = cache.get(userId);
            if (objFcmToken != null && objFcmToken.get() instanceof List<?> list) {
                if(!list.isEmpty() && list.getFirst() instanceof String) {
                    List<String> tokens = list
                            .stream()
                            .map(o -> (String) o)
                            .toList();
                    fcmTokens.addAll(tokens);
                }
            }
            else {
                userIdsMissing.add(userId);
            }
        }
        if(!userIdsMissing.isEmpty()){
            Map<Long, List<String>> userIdWithFcmTokens = tokenRepository
                    .findFcmTokeValueByUserIds(userIdsMissing)
                    .stream()
                    .collect(Collectors.groupingBy(UserIdWithFcmToken::userId,
                            Collectors.mapping(UserIdWithFcmToken::fcmToken,
                                    Collectors.toList())));
            userIdWithFcmTokens.forEach((aLong, strings) -> {
                cache.put(aLong, strings);
                fcmTokens.addAll(strings);
            });
            log.info("Added cache fcm token for {} user", userIdsMissing.size());

        }
        return new ArrayList<>(fcmTokens);
    }

    @CachePut(key = "#userId")
    @Override
    public List<String> putFcmTokenByUserId(Long userId, List<String> fcmTokens) {
        return fcmTokens;
    }
}
