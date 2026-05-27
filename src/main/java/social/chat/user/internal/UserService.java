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
import social.chat.user.UserMessage;

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
        userCache.updateUserCache(userId, null, null);
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }
}
