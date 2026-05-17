package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.VerificationType;
import social.chat.authentication.api.events.AuthRegisteredEvent;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.entity.Verification;
import social.chat.authentication.internal.repository.UserRepository;
import social.chat.authentication.internal.repository.VerificationRepository;
import social.chat.config.common.GlobalMessage;
import social.chat.config.common.Response;
import social.chat.config.common.ResponseTranslationAdvice;
import social.chat.exception.ConflictException;
import social.chat.profile.api.ProfileImp;
import social.chat.profile.api.dto.EmailResponse;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthService {
    VerificationRepository verificationRepository;
    ApplicationEventPublisher eventPublisher;
    ResponseTranslationAdvice responseTranslationAdvice;
    ProfileImp profileImp;
    UserRepository userRepository;

    @Transactional
    public Response<Void> sendVerificationEmail(String emailName){
        EmailResponse emailResponse = profileImp.getUserByEmail(emailName);
        if(emailResponse.getVerified() == true){
            throw new ConflictException(AuthenticationMessage.EmailSender.EMAIL_VERIFIED);
        }
        int expireAfterHour = 24;
        Verification verification = Verification.builder()
                .user(userRepository.findById(emailResponse.getUserId()).orElse(null))
                .verificationType(VerificationType.VERIFICATION_EMAIL)
                .typeId(emailResponse.getEmailId())
                .expiredAt(Instant.now().plus(expireAfterHour, ChronoUnit.HOURS))
                .build();
        verificationRepository.save(verification);
        String fullName = profileImp.getFullName(emailResponse.getUserId());
        AuthRegisteredEvent event = new AuthRegisteredEvent(emailName,
                responseTranslationAdvice.getString(AuthenticationMessage.SECURITY),
                fullName != null ? fullName : emailName,
                responseTranslationAdvice.getString(AuthenticationMessage.EmailSender.VERIFIED_EMAIL),
                "https://huyhoang271.id.vn",
                expireAfterHour + " " + responseTranslationAdvice.getString(GlobalMessage.Time.HOUR)
                );
        eventPublisher.publishEvent(event);
        return Response.success(
                AuthenticationMessage.EmailSender.SUCCESS,
                null
        );
    }
}
