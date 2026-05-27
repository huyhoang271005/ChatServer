package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.events.AuthUserIdsRegisteredEvent;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.profile.api.events.ProfileUserIdsRegisteredEvent;
import social.chat.user.api.dto.UserCacheDto;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.user.UserMessage;
import social.chat.user.api.UserImp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserLogicService implements UserImp {
    UserRepository userRepository;
    UserCache userCache;
    AuthenticationImp authenticationImp;
    PasswordEncoder passwordEncoder;
    AuthorizationImp authorizationImp;
    ApplicationEventPublisher applicationEventPublisher;

    @Override
    @Transactional(readOnly = true)
    public void checkUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ConflictException(UserMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.INACTIVE) {
            throw new ConflictException(UserMessage.User.NOT_VERIFIED);
        }

    }

    @Override
    @Transactional
    public Long getAndCreateUser() {
        return userRepository.save(User.builder()
                        .accountStatus(AccountStatus.PENDING_PROFILE)
                        .passwordHash(passwordEncoder.encode(UUID.randomUUID().toString()))
                        .roleId(authorizationImp.getRoleIdByRoleUser())
                        .build())
                .getUserId();
    }

    @Override
    @Transactional
    public void updateAccountStatusFromPendingToActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.PENDING_PROFILE) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
    }

    @Override
    @Transactional
    public Long getRoleIdAndCheckAccountStatus(Long userId) {
        UserCacheDto userCacheDto = userCache.getUserCache(userId);
        if(userCacheDto.getAccountStatus() != AccountStatus.ACTIVE) {
            switch (userCacheDto.getAccountStatus()) {
                case AccountStatus.BLOCKED ->
                        throw new ConflictException(UserMessage.Account.BLOCKED);
                case AccountStatus.INACTIVE ->
                        throw new ConflictException(UserMessage.Account.INACTIVE);
                default ->
                        throw new ConflictException(UserMessage.Account.INVALID);
            }
        }
        return userCacheDto.getRoleId();
    }

    @Override
    @Transactional
    public void updateUserRoleToRole(Long oldRoleId, Long newRoleId) {
        userRepository.updateRoleId(oldRoleId, newRoleId);
    }

    @Override
    @Transactional
    public void updateInactiveToPendingProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.INACTIVE) {
            user.setAccountStatus(AccountStatus.PENDING_PROFILE);
        }
    }

    @Override
    @Transactional
    public void updatePasswordHash(Long userId, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        user.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean checkPassword(Long userId, String password) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        return passwordEncoder.matches(password, user.getPasswordHash());
    }

    @Override
    public boolean checkUpdateProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        return user.getAccountStatus() != AccountStatus.PENDING_PROFILE;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isInactive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        return user.getAccountStatus() == AccountStatus.INACTIVE;
    }

    @Override
    @Transactional
    public void hardDeleteUser() {
        //Find users who deleted their accounts 7 days ago.
        List<Long> userIds = userRepository.findUserIdsExpired(Instant.now()
                .minus(7, ChronoUnit.DAYS));
        userRepository.deleteAllById(userIds);
        ProfileUserIdsRegisteredEvent profileUserIdsRegisteredEvent = new ProfileUserIdsRegisteredEvent(userIds);
        applicationEventPublisher.publishEvent(profileUserIdsRegisteredEvent);
        AuthUserIdsRegisteredEvent authUserIdsRegisteredEvent = new AuthUserIdsRegisteredEvent(userIds);
        applicationEventPublisher.publishEvent(authUserIdsRegisteredEvent);
    }
}
