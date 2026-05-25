package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.JwtResponse;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.exception.ConflictException;
import social.chat.user.api.dto.LoginRequest;
import social.chat.authentication.internal.entity.*;
import social.chat.user.api.UserImp;
import social.chat.authentication.internal.enums.TokenType;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.repository.*;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;
import social.chat.exception.UnauthorizedException;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.EmailResponse;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    SessionRepository sessionRepository;
    ProfileImp profileImp;
    DeviceRepository deviceRepository;
    TokenRepository tokenRepository;
    JwtService jwtService;
    UserImp userImp;

    @Transactional
    public Response<TokenDto> login(LoginRequest loginRequest, Long deviceId) {
        EmailResponse emailResponse = profileImp.getUserByEmail(loginRequest.getEmailName());
        Long userId = emailResponse.getUserId();
        boolean hasProfile = profileImp.existsProfileByUserId(userId);
        if(!userImp.checkPassword(userId, loginRequest.getPassword())) {
            throw new ConflictException(AuthenticationMessage.Validation.PASSWORD_INCORRECT);
        }
        TokenDto tokenDto = TokenDto.builder()
                .userId(String.valueOf(userId))
                .hasProfile(hasProfile)
                .verifiedEmail(emailResponse.getVerified())
                .verifiedDevice(false)
                .updateProfile(userImp.checkUpdateProfile(userId))
                .build();
        Optional.ofNullable(deviceId)
                .flatMap(deviceRepository::findById)
                .flatMap(device -> sessionRepository.findByDeviceAndUserId(device, userId))
                .ifPresent(session -> {
                    String refreshToken = jwtService.generateJwt(userId, session.getSessionId(), true);
                    Token token = tokenRepository.findBySessionAndTokenType(session, TokenType.REFRESH_TOKEN)
                                    .orElseGet(() -> Token.builder()
                                            .tokenType(TokenType.REFRESH_TOKEN)
                                            .build());
                    token.setTokenValue(refreshToken);
                    tokenRepository.save(token);
                    session.setLastLogin(Instant.now());
                    tokenDto.setAccessToken(jwtService.generateJwt(userId, session.getSessionId(), false));
                    tokenDto.setRefreshToken(refreshToken);
                    tokenDto.setVerifiedDevice(session.getValidated());
                });
        return Response.success(
                "Login success",
                tokenDto
        );
    }

    @Transactional
    public Response<TokenDto> refreshToken(String refreshToken) {
        JwtResponse jwtResponse;
        try {
            jwtResponse = jwtService.decoderJwt(refreshToken);
        } catch (Exception e) {
            tokenRepository.findByTokenValue(refreshToken).ifPresent(token -> {
                Session session = token.getSession();
                session.setRevoked(true);
            });
            log.error(e.getMessage());
            throw new UnauthorizedException(AuthenticationMessage.Session.EXPIRED);
        }
        TokenDto tokenDto = TokenDto.builder()
                .accessToken(jwtService.generateJwt(jwtResponse.getUserId(), jwtResponse.getSessionId(), false))
                .build();
        return Response.success(
                GlobalMessage.Success.CREATED,
                tokenDto
        );
    }
}
