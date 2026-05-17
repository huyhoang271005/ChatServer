package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.RoleDefault;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.repository.RoleRepository;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;
import social.chat.exception.ConflictException;
import social.chat.authentication.api.dto.AccountStatus;
import social.chat.authentication.api.dto.UserDto;
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
    ProfileImp profileImp;

    @Transactional
    public Response<UserDto> createUserWithEmail(UserDto userDto) {
        if(profileImp.existsByEmail(userDto.getEmail())) {
            throw new ConflictException(AuthenticationMessage.User.EXITS);
        }
        User user = User.builder()
                .accountStatus(AccountStatus.INACTIVE)
                .passwordHash(passwordEncoder.encode(userDto.getPassword()))
                .role(roleRepository.findByRoleName(RoleDefault.USER.name()).orElse(null))
                .build();
        userRepository.save(user);
        profileImp.createEmail(userDto.getEmail(), user.getUserId(), false);
        return Response.success(
                GlobalMessage.Success.CREATED,
                UserDto.builder()
                        .userId(user.getUserId().toString())
                        .build()
        );
    }

    @Transactional
    public Response<Void> deleteUser(Long userId) {
        softDeleteUser(List.of(userId));
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }

    @Transactional
    public void hardDeleteUser(List<Long> userIds) {
        userRepository.deleteAllById(userIds);
        profileImp.deleteProfileAndEmails(userIds);
    }

    @Transactional
    public void softDeleteUser(List<Long> userIds) {
        userRepository.softDelete(Instant.now(), userIds);
    }
}
