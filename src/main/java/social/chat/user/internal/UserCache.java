package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import social.chat.shared.common.GlobalParamName;
import social.chat.user.api.dto.UserCacheDto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CacheConfig(cacheNames = GlobalParamName.CacheName.USER)
public class UserCache {
    UserRepository userRepository;
    UserMapper userMapper;

    @Cacheable(key = "#userId")
    public UserCacheDto getUserCache(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user != null) {
            log.info("Cached user for user {}", userId);
            return userMapper.toUserCacheDto(user);
        }
        return null;
    }

    @CachePut(key = "#userId")
    public UserCacheDto updateUserCache(Long userId, Long roleId, AccountStatus accountStatus,
                                        Instant expireAt, boolean saveDb) {
        log.info("Updated cache for user {}", userId);
        if(saveDb) {
            userRepository.findById(userId)
                    .ifPresent(user -> {
                        if(roleId != null) {
                            user.setRoleId(roleId);
                        }
                        if(accountStatus != null) {
                            user.setAccountStatus(accountStatus);
                            if(accountStatus == AccountStatus.BANNED && expireAt != null) {
                                user.setExpireAt(expireAt);
                            }
                        }
                        userRepository.save(user);
                    });
            log.info("Db update user for user {}", userId);
        }
        return new UserCacheDto(roleId, accountStatus, expireAt);
    }

    @CacheEvict(key = "#userId")
    public void deleteUserCache(Long userId) {
        log.info("Deleted user for user {}", userId);
    }
}
