package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.profile.api.ProfileImp;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.user.UserMessage;
import social.chat.user.api.dto.ExtendUser;
import social.chat.user.api.dto.UserInfo;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserCache userCache;
    UserMapper userMapper;
    ProfileImp profileImp;
    AuthorizationImp authorizationImp;

    @Transactional
    public Response<UserInfo> createUserWithEmail(LoginRequest loginRequest) {
        if(profileImp.existsEmailByEmailName(loginRequest.emailName())) {
            throw new ConflictException(UserMessage.User.EXITS);
        }
        User user = User.builder()
                .accountStatus(AccountStatus.INACTIVE)
                .passwordHash(passwordEncoder.encode(loginRequest.password()))
                .roleId(authorizationImp.getRoleIdByRoleUser())
                .build();
        userRepository.save(user);
        profileImp.createEmail(loginRequest.emailName(), user.getUserId(), false);
        return Response.success(
                GlobalMessage.Success.CREATED,
                new UserInfo(user.getUserId(), null)
        );
    }

    @Transactional
    public Response<Void> SoftDeleteUser(Long userId) {
        userRepository.softDelete(Instant.now(), List.of(userId));
        userCache.deleteUserCache(userId);
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }

    public Response<ExtendUser> getExtendedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        return Response.success(
                GlobalMessage.Success.GET,
                userMapper.toExtendUser(user)
        );
    }

    @Transactional
    public Response<Void> updateExtendedUser(ExtendUser extendUser) {
        User user = userRepository.findById(extendUser.userId())
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        authorizationImp.existsRoleByRoleIdAndNotDelete(extendUser.roleId());
        if(extendUser.accountStatus() == AccountStatus.BANNED && (
        extendUser.expireAt() == null || extendUser.expireAt().isBefore(Instant.now()))){
            throw new ConflictException(UserMessage.Account.TIME_BANNED_INVALID,
                    String.valueOf(user.getUserId()));
        }
        if(extendUser.accountStatus() == AccountStatus.INACTIVE ||
        extendUser.accountStatus() == AccountStatus.PENDING_PROFILE) {
            throw new ConflictException(UserMessage.Account.INVALID);
        }
        userCache.updateUserCache(extendUser.userId(),
                extendUser.roleId(),
                extendUser.accountStatus(), extendUser.expireAt(), true);
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null
        );
    }
}
