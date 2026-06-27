package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.events.AuthenticationDeleteSessionByUserIdsRegisteredEvent;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.profile.api.events.ProfileAndEmailDeleteRegisteredEvent;
import social.chat.user.api.dto.UserCacheDto;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.user.UserMessage;
import social.chat.user.api.UserImp;
import social.chat.user.api.dto.UserInfo;
import social.chat.user.internal.cronjob.UserCronjobProperties;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserLogicService implements UserImp {
    UserRepository userRepository;
    UserCache userCache;
    PasswordEncoder passwordEncoder;
    AuthorizationImp authorizationImp;
    UserCronjobProperties userCronjobProperties;
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void checkUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ConflictException(UserMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.INACTIVE) {
            throw new ConflictException(UserMessage.User.NOT_VERIFIED);
        }

    }

    @Override
    public Long getAndCreateUser() {
        return userRepository.save(User.builder()
                        .accountStatus(AccountStatus.PENDING_PROFILE)
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .roleId(authorizationImp.getRoleIdByRoleUser())
                        .build())
                .getUserId();
    }

    @Override
    public void updateAccountStatusFromPendingToActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.PENDING_PROFILE) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
    }

    @Override
    public void updateAccountStatusToInactive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        user.setAccountStatus(AccountStatus.INACTIVE);
        userRepository.save(user);
    }

    @Override
    public Long getRoleIdAndCheckAccountStatus(Long userId) {
        UserCacheDto userCacheDto = userCache.getUserCache(userId);
        if(userCacheDto.accountStatus() != AccountStatus.ACTIVE &&
                userCacheDto.accountStatus() != AccountStatus.PENDING_PROFILE) {
            switch (userCacheDto.accountStatus()) {
                case LOCKED ->
                        throw new ConflictException(UserMessage.Account.LOCKED);
                case INACTIVE ->
                        throw new ConflictException(UserMessage.Account.INACTIVE);
                case BANNED -> {
                    if(userCacheDto.expireAt().isBefore(Instant.now())) {
                        userCache.updateUserCache(userId, userCacheDto.roleId(),
                                AccountStatus.ACTIVE, null, true);
                    } else {
                        throw new ConflictException(UserMessage.Account.BANNED, userCacheDto.expireAt());
                    }
                }
                default ->
                        throw new ConflictException(UserMessage.Account.INVALID);
            }
        }
        return userCacheDto.roleId();
    }

    @Override
    @Transactional
    public void updateUserRoleToRole(Long oldRoleId, Long newRoleId) {
        List<Long> userIds = userRepository.getUserIdsByRoleId(oldRoleId);
        int userUpdated = userRepository.updateRoleIdByUserIdIn(userIds, newRoleId);
        log.info("{} user updated role",  userUpdated);
        userIds.forEach(userCache::deleteUserCache);
    }

    @Override
    public void updateInactiveToPendingProfileOrActive(Long userId, boolean profileUpdated) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.INACTIVE) {
            if (profileUpdated) {
                user.setAccountStatus(AccountStatus.ACTIVE);
            } else {
                user.setAccountStatus(AccountStatus.PENDING_PROFILE);
            }
            userRepository.save(user);
        }
    }

    @Override
    public void updatePasswordHash(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public boolean checkPassword(Long userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    @Override
    public boolean isInactive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        return user.getAccountStatus() == AccountStatus.INACTIVE;
    }

    @Override
    @Transactional
    public void unbannedAccountCronjob() {
        List<UserInfo> userInfos = userRepository.findUserIdsNeedUnbanned(Instant.now());
        List<Long> userIds = userInfos.stream()
                .map(UserInfo::userId)
                .toList();
        int accountUnbanned = userRepository.unbannedByUserIds(userIds);
        log.info("{} account unbanned", accountUnbanned);
        userInfos.forEach(userInfo -> userCache.updateUserCache(userInfo.userId(), userInfo.roleId(),
                AccountStatus.ACTIVE, null, false));
    }

    @Override
    @Transactional
    public void hardDeleteUserCronjob() {
        List<Long> userIds = userRepository.findUserIdsExpired(Instant.now()
                .minus(userCronjobProperties.getDaysToKeepDeletedUser(), ChronoUnit.DAYS));
        userRepository.deleteAllById(userIds);
        ProfileAndEmailDeleteRegisteredEvent profileAndEmailDeleteRegisteredEvent = new ProfileAndEmailDeleteRegisteredEvent(userIds);
        applicationEventPublisher.publishEvent(profileAndEmailDeleteRegisteredEvent);
        AuthenticationDeleteSessionByUserIdsRegisteredEvent authenticationDeleteSessionByUserIdsRegisteredEvent = new AuthenticationDeleteSessionByUserIdsRegisteredEvent(userIds);
        applicationEventPublisher.publishEvent(authenticationDeleteSessionByUserIdsRegisteredEvent);
        log.info("{} user deleted by scheduled", userIds.size());
    }
}
