package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthImp;
import social.chat.authentication.api.dto.AccountStatus;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.entity.User;
import social.chat.authentication.internal.repository.UserRepository;
import social.chat.exception.ConflictException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthLogicService implements AuthImp {
    UserRepository userRepository;
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
}
