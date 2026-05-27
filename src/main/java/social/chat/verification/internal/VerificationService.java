package social.chat.verification.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.dto.AuthRegexValidation;
import social.chat.authentication.api.dto.SessionValidation;
import social.chat.verification.api.dto.VerificationDto;
import social.chat.verification.api.events.VerificationSendEmailRegisteredEvent;
import social.chat.user.api.UserImp;
import social.chat.verification.internal.enums.VerificationStatus;
import social.chat.verification.internal.enums.VerificationType;
import social.chat.shared.common.ApplicationProperties;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.common.ResponseTranslationAdvice;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.exception.UnprocessableException;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.EmailResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationService {
    VerificationRepository verificationRepository;
    ApplicationEventPublisher eventPublisher;
    ResponseTranslationAdvice responseTranslationAdvice;
    ProfileImp profileImp;
    UserImp userImp;
    AuthenticationImp authenticationImp;
    ApplicationProperties applicationProperties;

    @Transactional
    public Long sendEmailVerification(VerificationType verificationType, String title,
                                      String emailName, Long deviceId, String deviceName,
                                      String deviceType, String userAgent, String ipAddress,
                                      String location, int expireAfterHour, String webUrl) {
        EmailResponse emailResponse = profileImp.getUserByEmail(emailName);
        SessionValidation sessionValidation = authenticationImp.createSessionByDevice(emailResponse.getUserId(), deviceId, deviceName, deviceType,
                userAgent, ipAddress, location, true, false);
        Long typeId = getTypeId(verificationType, emailResponse, sessionValidation);
        List<Verification> verifications = verificationRepository
                .findBySessionIdAndVerificationTypeAndTypeIdAndExpiredAtAfterOrderByCreatedAtDesc(
                        sessionValidation.getSessionId(), verificationType, typeId, Instant.now()
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
                .sessionId(sessionValidation.getSessionId())
                .verificationType(verificationType)
                .typeId(typeId)
                .verificationStatus(VerificationStatus.PENDING)
                .expiredAt(Instant.now().plus(expireAfterHour, ChronoUnit.HOURS))
                .build();
        verificationRepository.save(verification);
        String fullName = profileImp.getFullName(emailResponse.getUserId());
        VerificationSendEmailRegisteredEvent event = new VerificationSendEmailRegisteredEvent(emailName,
                responseTranslationAdvice.getString(VerificationMessage.SECURITY),
                fullName != null ? fullName : emailName,
                responseTranslationAdvice.getString(title),
                applicationProperties.getFrontendUrl() + "/" + webUrl + "?verificationId=" +
                        verification.getVerificationId(),
                expireAfterHour + " " + responseTranslationAdvice.getString(GlobalMessage.Time.HOUR)
        );
        eventPublisher.publishEvent(event);
        return sessionValidation.getDeviceId();
    }

    private Long getTypeId(VerificationType verificationType, EmailResponse emailResponse, SessionValidation sessionValidation) {
        Long typeId;
        switch(verificationType) {
            case VERIFICATION_EMAIL -> {
                if(emailResponse.getVerified()) {
                    throw new ConflictException(VerificationMessage.Verification.EMAIL_VERIFIED);
                }
                typeId = emailResponse.getEmailId();
            }
            case VERIFICATION_DEVICE -> {
                if(sessionValidation.isValidated()) {
                    throw new ConflictException(VerificationMessage.Session.VERIFIED);
                }
                typeId = sessionValidation.getSessionId();
            }
            case VERIFICATION_RESET_PASSWORD -> typeId = sessionValidation.getUserId();
            default -> typeId = null;
        }
        return typeId;
    }

    @Transactional
    public Response<Long> sendVerificationEmail(String emailName, Long deviceId, String deviceName,
                                                String deviceType, String userAgent, String ipAddress,
                                                String location) {
        return Response.success(
                VerificationMessage.EmailSender.SUCCESS,
                sendEmailVerification(VerificationType.VERIFICATION_EMAIL,
                        VerificationMessage.Verification.EMAIL_VERIFICATION,
                        emailName, deviceId, deviceName, deviceType, userAgent, ipAddress,
                        location, 24, "#verify"),
                emailName
        );
    }

    @Transactional
    public Response<Long> sendVerificationDevice(String emailName, Long deviceId, String deviceName,
                                                 String deviceType, String userAgent, String ipAddress,
                                                 String location) {
        return Response.success(
                VerificationMessage.EmailSender.SUCCESS,
                sendEmailVerification(VerificationType.VERIFICATION_DEVICE,
                        VerificationMessage.Verification.DEVICE_VERIFICATION,
                        emailName, deviceId, deviceName, deviceType, userAgent, ipAddress,
                        location, 24, "#verify"),
                emailName
        );
    }

    @Transactional
    public Response<Long> sendVerificationChangePassword(String emailName, Long deviceId, String deviceName,
                                                         String deviceType, String userAgent, String ipAddress,
                                                         String location){
        return Response.success(
                VerificationMessage.EmailSender.SUCCESS,
                sendEmailVerification(VerificationType.VERIFICATION_RESET_PASSWORD,
                        VerificationMessage.Verification.RESET_PASSWORD_VERIFICATION,
                        emailName, deviceId, deviceName, deviceType, userAgent, ipAddress,
                        location, 1, "#reset-password"),
                emailName
        );
    }

    @Transactional
    public Response<Void> verify(VerificationDto verificationDto) {
        Verification verification = verificationRepository
                .findById(Long.parseLong(verificationDto.getVerificationId()))
                .orElseThrow(() -> new EntityNotFoundException(VerificationMessage.Verification.NOT_EXISTS));
        if(verification.getVerificationStatus() == VerificationStatus.USED){
            throw new ConflictException(VerificationMessage.Verification.USED);
        } else if(verification.getVerificationStatus() == VerificationStatus.CANCELLED){
            throw new ConflictException(VerificationMessage.Verification.INVALID);
        }
        if(verification.getExpiredAt().isBefore(Instant.now())){
            throw new ConflictException(VerificationMessage.Verification.EXPIRED);
        }
        Long userId = authenticationImp.getUserIdBySessionId(verification.getSessionId());
        switch (verification.getVerificationType()){
            case VERIFICATION_EMAIL -> {
                profileImp.verifiedEmail(verification.getTypeId());
                if(userImp.isInactive(userId)){
                    authenticationImp.updateValidatedSession(verification.getSessionId(), true);
                }
                userImp.updateInactiveToPendingProfile(userId);
            }
            case VERIFICATION_DEVICE -> authenticationImp.updateValidatedSession(verification.getSessionId(),
                    true);
            case VERIFICATION_RESET_PASSWORD -> {
                if(!verificationDto.getNewPassword().matches(AuthRegexValidation.PASSWORD)){
                    throw new UnprocessableException(VerificationMessage.Validation.PASSWORD_INVALID);
                }
                userImp.updatePasswordHash(userId, verificationDto.getNewPassword());
            }
            default -> {
                log.error("Invalid verification type {}", verification.getVerificationType());
                throw new RuntimeException(GlobalMessage.Error.INTERNAL);
            }
        }
        verification.setVerificationStatus(VerificationStatus.USED);
        verification.setUsedAt(Instant.now());
        return Response.success(
                VerificationMessage.Verification.SUCCESS,
                null
        );
    }
}
