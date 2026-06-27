package social.chat.authentication.internal.service;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.dto.FirebaseLoginRequest;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.authentication.api.dto.SessionValidation;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.cache.FcmTokenCache;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.entity.Token;
import social.chat.authentication.internal.enums.TokenType;
import social.chat.authentication.internal.repository.DeviceRepository;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.authentication.internal.repository.TokenRepository;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.EmailDto;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.security.JwtService;
import social.chat.user.api.UserImp;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    SessionRepository sessionRepository;
    ProfileImp profileImp;
    DeviceRepository deviceRepository;
    TokenRepository tokenRepository;
    JwtService jwtService;
    JwtDecoder jwtDecoder;
    UserImp userImp;
    AuthenticationImp authenticationImp;
    private final FcmTokenCache fcmTokenCache;

    @Transactional
    protected TokenDto createToken(Long deviceId, Long userId, String fcmToken, boolean verifiedEmail, Instant timeExpired) {
        boolean hasProfile = profileImp.existsProfileByUserId(userId);
        boolean updateProfile = profileImp.getUpdated(userId);
        return Optional.ofNullable(deviceId)
                .flatMap(deviceRepository::findById)
                .map(device -> {
                    Session session = sessionRepository.findByDeviceAndUserId(device, userId)
                            .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage
                                    .Session.NOT_EXISTS));
                    boolean verifiedDevice = session.getValidated();
                    String accessToken;
                    String refreshToken;
                    refreshToken = jwtService.generateJwt(userId, session.getSessionId(), true, timeExpired);
                    log.info("Generated refresh token");
                    Token tokenJwt = tokenRepository.findByDeviceAndTokenType(device, TokenType.REFRESH_TOKEN)
                            .orElseGet(() -> Token.builder()
                                    .tokenType(TokenType.REFRESH_TOKEN)
                                    .device(device)
                                    .build());
                    tokenJwt.setTokenValue(refreshToken);
                    Token tokenFcm = tokenRepository.findByDeviceAndTokenType(device, TokenType.FCM_TOKEN)
                            .orElse(null);
                    if(fcmToken != null){
                        if(tokenFcm != null) {
                            tokenFcm.setTokenValue(fcmToken);
                        }
                        else {
                            tokenFcm = Token.builder()
                                    .tokenType(TokenType.FCM_TOKEN)
                                    .device(device)
                                    .tokenValue(fcmToken)
                                    .build();
                            tokenRepository.save(tokenFcm);
                        }
                        List<String> fcmTokens = fcmTokenCache.getFcmTokenByUserIds(List.of(userId));
                        fcmTokens.add(fcmToken);
                        fcmTokenCache.putFcmTokenByUserId(userId, fcmTokens);
                    }
                    tokenRepository.save(tokenJwt);
                    session.setLastLogin(Instant.now());
                    session.setRevoked(false);
                    accessToken = jwtService.generateJwt(userId, session.getSessionId(), false, timeExpired);
                    log.info("Generated access token");
                    return new TokenDto(userId, deviceId, verifiedEmail, verifiedDevice, accessToken, refreshToken,
                            hasProfile, updateProfile);
                })
                .orElse(new TokenDto(userId, deviceId, verifiedEmail, false, null, null,
                        hasProfile, updateProfile));
    }

    @Transactional
    public Response<TokenDto> login(LoginRequest loginRequest, Long deviceId) {
        EmailDto emailDto = profileImp.getUserByEmail(loginRequest.emailName());
        Long userId = emailDto.userId();
        if(!userImp.checkPassword(userId, loginRequest.password())) {
            throw new ConflictException(AuthenticationMessage.Validation.PASSWORD_INCORRECT);
        }
        TokenDto tokenDto = createToken(deviceId, userId, loginRequest.fcmToken(), emailDto.verified(), null);
        return Response.success(
                "Login success",
                tokenDto
        );
    }

    @Transactional
    public Response<TokenDto> refreshToken(String refreshToken, Long deviceId, String ipAddress, String location) {
        Jwt jwt = jwtDecoder.decode(refreshToken);
        Long userId = Long.parseLong(jwt.getSubject());
        userImp.getRoleIdAndCheckAccountStatus(userId);
        authenticationImp.checkSession(jwt.getClaim(GlobalParamName.Jwt.SESSION_ID), ipAddress, location, true);
        TokenDto tokenDto = createToken(deviceId, userId, null, true, jwt.getExpiresAt());
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
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(firebaseLoginRequest
                    .firebaseToken());
            String email = decodedToken.getEmail();
            if(email == null || email.isEmpty()) {
                throw new ConflictException(AuthenticationMessage.Oauth2.NOT_FOUND_EMAIL);
            }
            Long userId = profileImp.getUserIdByEmail(email);
            SessionValidation sessionValidation = authenticationImp.createSessionByDevice(userId, deviceId, deviceName, deviceType, userAgent,
                    ipAddress, location, true, true);
            TokenDto tokenDto = createToken(sessionValidation.deviceId(), userId, firebaseLoginRequest.fcmToken(), true, null);
            return Response.success(
                    "Login Success",
                    tokenDto
            );
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
