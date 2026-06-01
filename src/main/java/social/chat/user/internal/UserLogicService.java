package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.user.api.dto.UserCacheDto;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.user.UserMessage;
import social.chat.user.api.UserImp;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserLogicService implements UserImp {
    UserRepository userRepository;
    UserCache userCache;
    PasswordEncoder passwordEncoder;
    AuthorizationImp authorizationImp;

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
    public void updateAccountStatusToInactive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        user.setAccountStatus(AccountStatus.INACTIVE);
    }

    @Override
    @Transactional
    public Long getRoleIdAndCheckAccountStatus(Long userId) {
        UserCacheDto userCacheDto = userCache.getUserCache(userId);
        if(userCacheDto.getAccountStatus() != AccountStatus.ACTIVE &&
                userCacheDto.getAccountStatus() != AccountStatus.PENDING_PROFILE) {
            switch (userCacheDto.getAccountStatus()) {
                case LOCKED ->
                        throw new ConflictException(UserMessage.Account.LOCKED);
                case INACTIVE ->
                        throw new ConflictException(UserMessage.Account.INACTIVE);
                case BANNED ->
                    throw new ConflictException(UserMessage.Account.BANNED, userCacheDto.getExpireAt());
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
    public void updateInactiveToPendingProfileOrActive(Long userId, boolean profileUpdated) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.INACTIVE) {
            if (profileUpdated) {
                user.setAccountStatus(AccountStatus.ACTIVE);
            } else {
                user.setAccountStatus(AccountStatus.PENDING_PROFILE);
            }
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
    @Transactional(readOnly = true)
    public boolean isInactive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        return user.getAccountStatus() == AccountStatus.INACTIVE;
    }
}
