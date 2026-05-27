package social.chat.authentication.internal.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.dto.FirebaseLoginRequest;
import social.chat.authentication.api.dto.JwtResponse;
import social.chat.authentication.api.dto.SessionValidation;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.shared.exception.ConflictException;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.authentication.internal.entity.*;
import social.chat.user.api.UserImp;
import social.chat.authentication.internal.enums.TokenType;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.repository.*;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.UnauthorizedException;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.EmailResponse;

import java.time.Instant;
import java.util.List;
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
    AuthenticationImp authenticationImp;

    private void createToken(Long deviceId, Long userId, String fcmToken, TokenDto tokenDto) {
        Optional.ofNullable(deviceId)
                .flatMap(deviceRepository::findById)
                .flatMap(device -> sessionRepository.findByDeviceAndUserId(device, userId))
                .ifPresent(session -> {
                    String refreshToken = jwtService.generateJwt(userId, session.getSessionId(), true);
                    Token tokenJwt = tokenRepository.findBySessionAndTokenType(session, TokenType.REFRESH_TOKEN)
                            .orElseGet(() -> Token.builder()
                                    .tokenType(TokenType.REFRESH_TOKEN)
                                    .session(session)
                                    .build());
                    tokenJwt.setTokenValue(refreshToken);
                    Token tokenFcm = tokenRepository.findBySessionAndTokenType(session, TokenType.FCM_TOKEN)
                            .orElseGet(() -> Token.builder()
                                    .tokenType(TokenType.FCM_TOKEN)
                                    .session(session)
                                    .build());
                    tokenFcm.setTokenValue(fcmToken);
                    tokenRepository.saveAll(List.of(tokenJwt, tokenFcm));
                    session.setLastLogin(Instant.now());
                    tokenDto.setAccessToken(jwtService.generateJwt(userId, session.getSessionId(), false));
                    tokenDto.setRefreshToken(refreshToken);
                    tokenDto.setVerifiedDevice(session.getValidated());
                });
    }

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
        createToken(deviceId, userId, loginRequest.getFcmToken(), tokenDto);
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

    @Transactional
    public Response<TokenDto> oauth2Login(FirebaseLoginRequest firebaseLoginRequest, Long deviceId,
                                          String deviceName, String deviceType, String userAgent,
                                          String ipAddress, String location) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseLoginRequest.getFirebaseToken());
            String email = decodedToken.getEmail();
            if(email == null || email.isEmpty()) {
                throw new ConflictException(AuthenticationMessage.Oauth2.NOT_FOUND_EMAIL);
            }
            Long userId = profileImp.getUserIdByEmail(email);
            SessionValidation sessionValidation = authenticationImp.createSessionByDevice(userId, deviceId, deviceName, deviceType, userAgent,
                    ipAddress, location, true, true);
            TokenDto tokenDto = TokenDto.builder()
                    .userId(String.valueOf(userId))
                    .hasProfile(profileImp.existsProfileByUserId(userId))
                    .updateProfile(userImp.checkUpdateProfile(userId))
                    .verifiedEmail(true)
                    .verifiedDevice(true)
                    .deviceId(String.valueOf(sessionValidation.getDeviceId()))
                    .build();
            createToken(deviceId, userId, firebaseLoginRequest.getFcmToken(), tokenDto);
            return Response.success(
                    "Login Success",
                    tokenDto
            );
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
