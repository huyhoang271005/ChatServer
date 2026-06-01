package social.chat.user.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.ConflictException;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.profile.api.ProfileImp;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.user.UserMessage;
import social.chat.user.api.dto.ExtendUser;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    UserCache userCache;
    ProfileImp profileImp;
    AuthorizationImp authorizationImp;

    @Transactional
    public Response<TokenDto> createUserWithEmail(LoginRequest loginRequest) {
        if(profileImp.existsEmailByEmailName(loginRequest.getEmailName())) {
            throw new ConflictException(UserMessage.User.EXITS);
        }
        User user = User.builder()
                .accountStatus(AccountStatus.INACTIVE)
                .passwordHash(passwordEncoder.encode(loginRequest.getPassword()))
                .roleId(authorizationImp.getRoleIdByRoleUser())
                .build();
        userRepository.save(user);
        profileImp.createEmail(loginRequest.getEmailName(), user.getUserId(), false);
        return Response.success(
                GlobalMessage.Success.CREATED,
                TokenDto.builder()
                        .userId(String.valueOf(user.getUserId()))
                        .build()
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

    @Transactional(readOnly = true)
    public Response<ExtendUser> getExtendedUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        return Response.success(
                GlobalMessage.Success.GET,
                ExtendUser.builder()
                        .userId(String.valueOf(user.getUserId()))
                        .accountStatus(user.getAccountStatus())
                        .roleId(String.valueOf(user.getRoleId()))
                        .build()
        );
    }

    @Transactional
    public Response<Void> updateExtendedUser(ExtendUser extendUser) {
        User user = userRepository.findById(Long.parseLong(extendUser.getUserId()))
                .orElseThrow(() -> new EntityNotFoundException(UserMessage.User.NOT_EXITS));
        authorizationImp.existsRoleByRoleIdAndNotDelete(Long.parseLong(extendUser.getRoleId()));
        if(extendUser.getAccountStatus() == AccountStatus.BANNED && (
        extendUser.getExpireAt() == null || extendUser.getExpireAt().isBefore(Instant.now()))){
            throw new ConflictException(UserMessage.Account.TIME_BANNED_INVALID,
                    String.valueOf(user.getUserId()));
        }
        if(extendUser.getAccountStatus() == AccountStatus.INACTIVE ||
        extendUser.getAccountStatus() == AccountStatus.PENDING_PROFILE) {
            throw new ConflictException(UserMessage.Account.INVALID);
        }
        userCache.updateUserCache(Long.parseLong(extendUser.getUserId()),
                Long.parseLong(extendUser.getRoleId()),
                extendUser.getAccountStatus(), extendUser.getExpireAt(), true);
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null
        );
    }
}
