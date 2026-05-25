package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.user.api.dto.UserCacheDto;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserCache {
    UserRepository userRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserCacheDto getUserCache(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user != null) {
            log.info("Cached user for user {}", userId);
            return UserCacheDto.builder()
                    .roleId(user.getRoleId())
                    .accountStatus(user.getAccountStatus())
                    .build();
        }
        return null;
    }

    @Transactional
    @CacheEvict(value = "users", key = "#userId")
    public void updateUserCache(Long userId, Long roleId, AccountStatus accountStatus) {
        log.info("Deleted cache for user {}", userId);
        userRepository.findById(userId)
                .ifPresent(user -> {
                    if(roleId != null) {
                        user.setRoleId(roleId);
                    }
                    if(accountStatus != null) {
                        user.setAccountStatus(accountStatus);
                    }
                });
        log.info("Db update user for user {}", userId);
    }
}
