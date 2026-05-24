package social.chat.authentication.internal.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.internal.enums.AccountStatus;
import social.chat.authentication.internal.enums.RoleDefault;
import social.chat.authentication.api.dto.UserCacheDto;
import social.chat.authentication.internal.entity.User;
import social.chat.authentication.internal.repository.RoleRepository;
import social.chat.authentication.internal.repository.UserRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserCache {
    UserRepository userRepository;
    RoleRepository roleRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "users", key = "#userId")
    public UserCacheDto getUserCache(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if(user != null) {
            log.info("Cached user for user {}", userId);
            return UserCacheDto.builder()
                    .roleId(user.getRole().getRoleId())
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
                        user.setRole(roleRepository.findById(roleId)
                                .orElse(roleRepository.findByRoleName(RoleDefault.USER.name())
                                        .orElse(null)));
                    }
                    if(accountStatus != null) {
                        user.setAccountStatus(accountStatus);
                    }
                });
        log.info("Db update user for user {}", userId);
    }
}
