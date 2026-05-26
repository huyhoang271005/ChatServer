package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.repository.DeviceRepository;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.authentication.internal.repository.VerificationRepository;
import social.chat.exception.ConflictException;
import social.chat.exception.EntityNotFoundException;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationLogicService implements AuthenticationImp {
    VerificationRepository verificationRepository;
    JwtService jwtService;
    SessionRepository sessionRepository;
    DeviceRepository deviceRepository;

    @Override
    public TokenDto generateToken(Long userId, Long deviceId) {
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS));
        Long sessionId = sessionRepository.findByDeviceAndUserId(device, userId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS))
                .getSessionId();
        return TokenDto.builder()
                .accessToken(jwtService.generateJwt(userId, sessionId, false))
                .refreshToken(jwtService.generateJwt(userId, sessionId, true))
                .hasProfile(true)
                .build();
    }

    @Override
    @Transactional
    public void hardDeleteSessionByUserIds(List<Long> userIds) {
        sessionRepository.deleteByUserIdIn(userIds);
    }

    @Override
    @Transactional
    public void expiredVerification() {
        verificationRepository.expireVerificationPending(Instant.now());
    }

    @Override
    @Transactional
    public void hardDeleteVerification() {
        throw new ConflictException("Update soon");
    }
}
