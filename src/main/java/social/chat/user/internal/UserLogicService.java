package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.user.api.dto.UserCacheDto;
import social.chat.exception.ConflictException;
import social.chat.exception.EntityNotFoundException;
import social.chat.profile.api.ProfileImp;
import social.chat.user.UserMessage;
import social.chat.user.api.UserImp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserLogicService implements UserImp {
    UserRepository userRepository;
    UserCache userCache;
    ProfileImp profileImp;
    AuthenticationImp authenticationImp;
    PasswordEncoder passwordEncoder;

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
        profileImp.deleteProfileAndEmails(userIds);
        authenticationImp.hardDeleteSessionByUserIds(userIds);
    }
}
