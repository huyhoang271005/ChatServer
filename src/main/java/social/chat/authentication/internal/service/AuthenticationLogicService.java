package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.JwtProperties;
import social.chat.authentication.api.dto.SessionCacheDto;
import social.chat.authentication.api.dto.SessionValidation;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.cache.SessionCache;
import social.chat.authentication.internal.cronjob.AuthenticationCronjobProperties;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.repository.DeviceRepository;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.authentication.internal.repository.TokenRepository;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.exception.UnauthorizedException;
import social.chat.shared.security.JwtService;
import social.chat.verification.api.events.VerificationDeleteBySessionIdsRegisteredEvent;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationLogicService implements AuthenticationImp {
    JwtService jwtService;
    SessionRepository sessionRepository;
    DeviceRepository deviceRepository;
    SessionCache sessionCache;
    ApplicationEventPublisher applicationEventPublisher;
    JwtProperties jwtProperties;
    AuthenticationCronjobProperties authenticationCronjobProperties;
    TokenRepository tokenRepository;

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

    @Override
    @Transactional
    public void deleteSessionByUserIds(List<Long> userIds) {
        List<Long> sessionIds = sessionRepository.findSessionIdsByUserIds(userIds);
        sessionRepository.deleteAllById(sessionIds);
        sessionIds.forEach(sessionId -> sessionCache
                .evictCacheSession(sessionId, null, false));
        VerificationDeleteBySessionIdsRegisteredEvent verificationDeleteBySessionIdsRegisteredEvent =
                new VerificationDeleteBySessionIdsRegisteredEvent(sessionIds);
        applicationEventPublisher.publishEvent(verificationDeleteBySessionIdsRegisteredEvent);
    }

    @Override
    @Transactional
    public void checkSession(Long sessionId, String ipAddress, String location, boolean saveDb) {
        SessionCacheDto sessionCacheDto = sessionCache.getCacheSession(sessionId);
        if(sessionCacheDto.isRevoked()){
            throw new UnauthorizedException(AuthenticationMessage.Session.EXPIRED);
        }
        sessionCache.putCacheSession(sessionId, false, sessionCacheDto.getIpAddress(),
                ipAddress, location, saveDb);
    }

    @Override
    @Transactional
    public void revokedSessionExpiredCron() {
        List<Long> sessionIds = sessionRepository.findSessionIdsByTimeMinus(Instant.now()
                .minusSeconds(jwtProperties.getRefreshTokenExpire()));
        int sessionRevoked = sessionRepository.revokeSessionBySessionIds(sessionIds);
        log.info("{} sessions revoked by scheduled", sessionRevoked);
        sessionIds.forEach(sessionId -> sessionCache.evictCacheSession(sessionId, null, false));
    }

    @Override
    @Transactional
    public void cleanupDeviceCron() {
        int deviceDeleted = deviceRepository.deleteBySessionsIsEmpty();
        log.info("{} devices deleted by scheduled", deviceDeleted);
    }

    @Override
    @Transactional
    public void cleanupSessionCron() {
        List<Long> sessionIds = sessionRepository.findSessionIdByLastLoginBefore(Instant.now()
                .minusSeconds(authenticationCronjobProperties
                        .getDaysToKeepSessionExpired()));
        int sessionDeleted = sessionRepository.deleteBySessionIdIn(sessionIds);
        applicationEventPublisher
                .publishEvent(new VerificationDeleteBySessionIdsRegisteredEvent(sessionIds));
        log.info("{} sessions deleted by scheduled", sessionDeleted);
    }

    @Override
    @Transactional
    public List<String> getFcmTokenByUserIds(List<Long> userIds) {
        return tokenRepository.findFcmTokeValueByUserIds(userIds);
    }
}
