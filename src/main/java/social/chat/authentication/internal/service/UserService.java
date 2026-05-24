package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.cache.UserCache;
import social.chat.authentication.internal.enums.RoleDefault;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.repository.RoleRepository;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;
import social.chat.exception.ConflictException;
import social.chat.authentication.internal.enums.AccountStatus;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.authentication.internal.entity.User;
import social.chat.authentication.internal.repository.UserRepository;
import social.chat.profile.api.ProfileImp;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserService {
    UserRepository userRepository;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;
    UserCache userCache;
    ProfileImp profileImp;

    @Transactional
    public Response<TokenDto> createUserWithEmail(LoginRequest loginRequest) {
        if(profileImp.existsEmailByEmailName(loginRequest.getEmailName())) {
            throw new ConflictException(AuthenticationMessage.User.EXITS);
        }
        User user = User.builder()
                .accountStatus(AccountStatus.INACTIVE)
                .passwordHash(passwordEncoder.encode(loginRequest.getPassword()))
                .role(roleRepository.findByRoleName(RoleDefault.USER.name()).orElse(null))
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
    public Response<Void> deleteUser(Long userId) {
        softDeleteUser(List.of(userId));
        userCache.updateUserCache(userId, null, null);
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }

    @Transactional
    public void softDeleteUser(List<Long> userIds) {
        userRepository.softDelete(Instant.now(), userIds);
    }
}
