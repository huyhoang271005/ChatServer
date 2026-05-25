package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.AuthRegexValidation;
import social.chat.authentication.api.dto.VerificationDto;
import social.chat.authentication.api.events.AuthRegisteredEvent;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.entity.User;
import social.chat.authentication.internal.entity.Verification;
import social.chat.authentication.internal.enums.AccountStatus;
import social.chat.authentication.internal.enums.VerificationStatus;
import social.chat.authentication.internal.enums.VerificationType;
import social.chat.authentication.internal.repository.*;
import social.chat.config.common.ApplicationProperties;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;
import social.chat.config.common.ResponseTranslationAdvice;
import social.chat.exception.ConflictException;
import social.chat.exception.EntityNotFoundException;
import social.chat.exception.UnprocessableException;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.EmailResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationService {
    VerificationRepository verificationRepository;
    SessionRepository sessionRepository;
    ApplicationEventPublisher eventPublisher;
    ResponseTranslationAdvice responseTranslationAdvice;
    ProfileImp profileImp;
    UserRepository userRepository;
    DeviceRepository deviceRepository;
    ApplicationProperties applicationProperties;
    PasswordEncoder passwordEncoder;

    @Transactional
    protected Session createSession(Long userId, Long deviceId, String deviceName, String deviceType,
                                    String userAgent, String ipAddress, String location) {
        log.info("Device id is {}", deviceId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.User.NOT_EXITS));
        Device device = Optional.ofNullable(deviceId)
                .flatMap(deviceRepository::findById)
                .orElseGet(() -> deviceRepository.save(Device.builder()
                        .deviceName(deviceName)
                        .deviceType(deviceType)
                        .userAgent(userAgent)
                        .build()));
        return sessionRepository.findByDeviceAndUser(device, user).orElseGet(() ->
                sessionRepository.save(Session.builder()
                        .revoked(true)
                        .user(user)
                        .ipAddress(ipAddress)
                        .device(device)
                        .validated(false)
                        .location(location)
                        .build()));
    }

    @Transactional
    public Long sendEmailVerification(VerificationType verificationType, String title,
                                      String emailName, Long deviceId, String deviceName,
                                      String deviceType, String userAgent, String ipAddress,
                                      String location, int expireAfterHour, String webUrl) {
        EmailResponse emailResponse = profileImp.getUserByEmail(emailName);
        Session session = createSession(emailResponse.getUserId(), deviceId, deviceName, deviceType,
                userAgent, ipAddress, location);
        Long typeId = getTypeId(verificationType, emailResponse, session);
        List<Verification> verifications = verificationRepository
                .findBySessionAndVerificationTypeAndTypeIdAndExpiredAtAfterOrderByCreatedAtDesc(
                        session, verificationType, typeId, Instant.now()
                );
        if(!verifications.isEmpty()) {
            long timeWaited = Duration.between(verifications.getFirst().getCreatedAt(),
                    Instant.now()).toMinutes();
            double timeNeedWait = Math.pow(verifications.size(), 2);
            if(timeWaited < timeNeedWait) {
                throw new ConflictException(GlobalMessage.RateLimit.MINUTE, timeNeedWait - timeWaited);
            }
        }
        verificationRepository.cancelVerificationPending(typeId, Instant.now());
        Verification verification = Verification.builder()
                .session(session)
                .verificationType(verificationType)
                .typeId(typeId)
                .verificationStatus(VerificationStatus.PENDING)
                .expiredAt(Instant.now().plus(expireAfterHour, ChronoUnit.HOURS))
                .build();
        verificationRepository.save(verification);
        String fullName = profileImp.getFullName(emailResponse.getUserId());
        AuthRegisteredEvent event = new AuthRegisteredEvent(emailName,
                responseTranslationAdvice.getString(AuthenticationMessage.SECURITY),
                fullName != null ? fullName : emailName,
                responseTranslationAdvice.getString(title),
                applicationProperties.getFrontendUrl() + "/" + webUrl + "?verificationId=" +
                        verification.getVerificationId(),
                expireAfterHour + " " + responseTranslationAdvice.getString(GlobalMessage.Time.HOUR)
        );
//        eventPublisher.publishEvent(event);
        return session.getDevice().getDeviceId();
    }

    private Long getTypeId(VerificationType verificationType, EmailResponse emailResponse, Session session) {
        Long typeId;
        switch(verificationType) {
            case VERIFICATION_EMAIL -> {
                if(!emailResponse.getVerified()) {
                    throw new ConflictException(AuthenticationMessage.Verification.EMAIL_VERIFIED);
                }
                typeId = emailResponse.getEmailId();
            }
            case VERIFICATION_DEVICE -> {
                if(session.getValidated()) {
                    throw new ConflictException(AuthenticationMessage.Verification.DEVICE_VERIFIED);
                }
                typeId = session.getSessionId();
            }
            case VERIFICATION_RESET_PASSWORD -> typeId = session.getUser().getUserId();
            default -> typeId = null;
        }
        return typeId;
    }

    @Transactional
    public Response<Long> sendVerificationEmail(String emailName, Long deviceId, String deviceName,
                                                String deviceType, String userAgent, String ipAddress,
                                                String location) {
        return Response.success(
                AuthenticationMessage.EmailSender.SUCCESS,
                sendEmailVerification(VerificationType.VERIFICATION_DEVICE,
                        AuthenticationMessage.Verification.EMAIL_VERIFICATION,
                        emailName, deviceId, deviceName, deviceType, userAgent, ipAddress,
                        location, 24, "#verify")
        );
    }

    @Transactional
    public Response<Long> sendVerificationDevice(String emailName, Long deviceId, String deviceName,
                                                 String deviceType, String userAgent, String ipAddress,
                                                 String location) {
        return Response.success(
                AuthenticationMessage.EmailSender.SUCCESS,
                sendEmailVerification(VerificationType.VERIFICATION_DEVICE,
                        AuthenticationMessage.Verification.DEVICE_VERIFICATION,
                        emailName, deviceId, deviceName, deviceType, userAgent, ipAddress,
                        location, 24, "#verify")
        );
    }

    @Transactional
    public Response<Long> sendVerificationChangePassword(String emailName, Long deviceId, String deviceName,
                                                         String deviceType, String userAgent, String ipAddress,
                                                         String location){
        return Response.success(
                AuthenticationMessage.EmailSender.SUCCESS,
                sendEmailVerification(VerificationType.VERIFICATION_RESET_PASSWORD,
                        AuthenticationMessage.Verification.RESET_PASSWORD_VERIFICATION,
                        emailName, deviceId, deviceName, deviceType, userAgent, ipAddress,
                        location, 1, "#reset-password")
        );
    }

    @Transactional
    public Response<Void> verify(VerificationDto verificationDto) {
        Verification verification = verificationRepository
                .findById(Long.parseLong(verificationDto.getVerificationId()))
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Verification.NOT_EXISTS));
        if(verification.getVerificationStatus() == VerificationStatus.USED){
            throw new ConflictException(AuthenticationMessage.Verification.USED);
        } else if(verification.getVerificationStatus() == VerificationStatus.CANCELLED){
            throw new ConflictException(AuthenticationMessage.Verification.INVALID);
        }
        if(verification.getExpiredAt().isBefore(Instant.now())){
            throw new ConflictException(AuthenticationMessage.Verification.EXPIRED);
        }
        User user = userRepository.findByVerificationId(Long.parseLong(verificationDto.getVerificationId()))
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.User.NOT_EXITS));
        switch (verification.getVerificationType()){
            case VERIFICATION_EMAIL -> {
                profileImp.verifiedEmail(verification.getTypeId());
                if(user.getAccountStatus() == AccountStatus.INACTIVE){
                    user.setAccountStatus(AccountStatus.PENDING_PROFILE);
                }
            }
            case VERIFICATION_DEVICE -> {
                Session session = sessionRepository.findById(verification.getTypeId())
                        .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS));
                session.setValidated(true);
            }
            case VERIFICATION_RESET_PASSWORD -> {
                if(!verificationDto.getNewPassword().matches(AuthRegexValidation.PASSWORD)){
                    throw new UnprocessableException(AuthenticationMessage.Validation.PASSWORD_INVALID);
                }
                user.setPasswordHash(passwordEncoder.encode(verificationDto.getNewPassword()));
            }
            default -> {
                log.error("Invalid verification type {}", verification.getVerificationType());
                throw new RuntimeException(GlobalMessage.Error.INTERNAL);
            }
        }
        verification.setVerificationStatus(VerificationStatus.USED);
        verification.setUsedAt(Instant.now());
        return Response.success(
                AuthenticationMessage.Verification.SUCCESS,
                null
        );
    }
}
