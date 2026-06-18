package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.user.api.dto.UserCacheDto;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserCache {
    UserRepository userRepository;
    UserMapper userMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserCacheDto getUserCache(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user != null) {
            log.info("Cached user for user {}", userId);
            return userMapper.toUserCacheDto(user);
        }
        return null;
    }

    @Transactional
    @CachePut(value = "users", key = "#userId")
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
                    });
            log.info("Db update user for user {}", userId);
        }
        return new UserCacheDto(roleId, accountStatus, expireAt);
    }

    @CacheEvict(cacheNames = "users", key = "#userId")
    public void deleteUserCache(Long userId) {
        log.info("Deleted user for user {}", userId);
    }
}
