package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthImp;
import social.chat.authentication.internal.enums.AccountStatus;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.entity.User;
import social.chat.authentication.internal.repository.RoleRepository;
import social.chat.authentication.internal.repository.UserRepository;
import social.chat.exception.ConflictException;
import social.chat.exception.EntityNotFoundException;
import social.chat.profile.api.ProfileImp;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthLogicService implements AuthImp {
    UserRepository userRepository;
    RoleRepository roleRepository;
    ProfileImp profileImp;
    JwtService jwtService;

    @Override
    @Transactional(readOnly = true)
    public void checkUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new ConflictException(AuthenticationMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.INACTIVE) {
            throw new ConflictException(AuthenticationMessage.User.NOT_VERIFIED);
        }

    }

    @Override
    public TokenDto generateToken(Long userId, Long sessionId) {
        return TokenDto.builder()
                .accessToken(jwtService.generateJwt(userId, sessionId, false))
                .refreshToken(jwtService.generateJwt(userId, sessionId, true))
                .hasProfile(true)
                .build();
    }

    @Override
    @Transactional
    public void updateAccountStatusFromPendingToActive(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.User.NOT_EXITS));
        if(user.getAccountStatus() == AccountStatus.PENDING_PROFILE) {
            user.setAccountStatus(AccountStatus.ACTIVE);
        }
    }

    @Override
    @Transactional
    public void hardDeleteRole() {
        //The role was deleted 7 days ago.
        roleRepository.deleteRolesWithTimeExpired(Instant.now()
                .minus(7, ChronoUnit.DAYS));
    }

    @Override
    @Transactional
    public void hardDeleteUser() {
        //Find users who deleted their accounts 7 days ago.
        List<Long> userIds = userRepository.findUserIdsExpired(Instant.now()
                .minus(7, ChronoUnit.DAYS));
        userRepository.deleteAllById(userIds);
        profileImp.deleteProfileAndEmails(userIds);
    }
}
