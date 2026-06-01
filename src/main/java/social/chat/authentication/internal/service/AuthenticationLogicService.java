package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.dto.SessionValidation;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.repository.DeviceRepository;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.security.JwtService;
import social.chat.verification.api.VerificationImp;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationLogicService implements AuthenticationImp {
    JwtService jwtService;
    SessionRepository sessionRepository;
    DeviceRepository deviceRepository;
    VerificationImp verificationImp;

    @Override
    @Transactional
    public SessionValidation createSessionByDevice(Long userId, Long deviceId, String deviceName, String deviceType,
                                                   String userAgent, String ipAddress, String location,
                                                   boolean revoked, boolean validated) {
        log.info("Device id is {}", deviceId);
        Device device = Optional.ofNullable(deviceId)
                .flatMap(deviceRepository::findById)
                .orElseGet(() -> deviceRepository.save(Device.builder()
                        .deviceName(deviceName)
                        .deviceType(deviceType)
                        .userAgent(userAgent)
                        .build()));
        Session session = sessionRepository.findByDeviceAndUserId(device, userId).orElseGet(() ->
                sessionRepository.save(Session.builder()
                        .revoked(revoked)
                        .userId(userId)
                        .ipAddress(ipAddress)
                        .device(device)
                        .validated(validated)
                        .location(location)
                        .build()));
        return SessionValidation.builder()
                .sessionId(session.getSessionId())
                .deviceId(device.getDeviceId())
                .validated(session.getValidated())
                .build();
    }

    @Override
    public TokenDto generateToken(Long userId, Long deviceId, Instant timeExpired) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS));
        Long sessionId = sessionRepository.findByDeviceAndUserId(device, userId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS))
                .getSessionId();
        return TokenDto.builder()
                .accessToken(jwtService.generateJwt(userId, sessionId, false, timeExpired))
                .refreshToken(jwtService.generateJwt(userId, sessionId, true, timeExpired))
                .hasProfile(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Long getUserIdBySessionId(Long sessionId) {
        return sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS))
                .getUserId();
    }

    @Override
    public ResponseCookie getResponseCookie(String paramName, String paramValue, Duration duration) {
        return ResponseCookie.from(paramName, paramValue)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(duration)
                .build();
    }

    @Override
    @Transactional
    public void updateValidatedSession(Long sessionId, boolean validated) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS));
        if(session.getValidated() != validated){
            session.setValidated(validated);
        }
    }
}
