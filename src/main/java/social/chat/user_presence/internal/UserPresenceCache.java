package social.chat.user_presence.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import social.chat.shared.cache.SafeCacheExecutor;
import social.chat.shared.common.GlobalParamName;
import social.chat.user_presence.api.UserPresenceDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CacheConfig(cacheNames = GlobalParamName.CacheName.USER_PRESENCE)
public class UserPresenceCache {
    Set<Long> userIdsPending = ConcurrentHashMap.newKeySet();
    SafeCacheExecutor safeCacheExecutor;
    Lock userPresenceLock = new ReentrantLock();
    UserPresenceRepository userPresenceRepository;
    UserPresenceMapper userPresenceMapper;

    public Optional<List<UserPresenceDto>> getPresencesCache(Collection<Long> userIds) {
        return safeCacheExecutor.getCacheByIds(
                userIds, GlobalParamName.CacheName.USER_PRESENCE,
                UserPresenceDto.class, userPresenceLock, longs ->
                        userPresenceRepository.findAllById(longs)
                                .stream().map(userPresenceMapper::toUserPresenceDto)
                                .toList(), UserPresenceDto::userId
        );
    }

    public Collection<Long> getAllUserIdsPending() {
        return safeCacheExecutor.getAllPendingIds(userIdsPending);
    }

    @CachePut(key = "#userId")
    public UserPresenceDto updateUserPresence(Long userId, UserPresenceDto userPresenceDto) {
        userIdsPending.add(userId);
        return userPresenceDto;
    }

    public Collection<Long> getBatchUserIdsPending() {
        return safeCacheExecutor.getBatchPendingIds(userIdsPending, 100);
    }

    public void saveData(Collection<Long> userIds) {
        safeCacheExecutor.saveDataWithIds(userIds, userIdsPending,
                this::getPresencesCache, userPresenceDtos ->
                        userPresenceRepository.saveAll(userPresenceDtos
                                .stream()
                                .map(userPresenceMapper::toUserPresence)
                                .toList())
                );
    }

}
