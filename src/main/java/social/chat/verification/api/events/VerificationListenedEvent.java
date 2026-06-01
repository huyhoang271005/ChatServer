package social.chat.verification.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.shared.emailSender.EmailSenderService;
import social.chat.shared.common.ApplicationProperties;
import social.chat.verification.internal.VerificationRepository;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationListenedEvent {
    EmailSenderService emailSenderService;
    ApplicationProperties applicationProperties;
    VerificationRepository verificationRepository;

    public void sendEmailVerify(String toEmail, String title, String fullName, String activity,
                                String verificationUrl, String timeExpire) {
        emailSenderService.sendEmail(toEmail, title, "verified", Map.of(
                "appName", applicationProperties.getName(),
                "fullName", fullName,
                "activity", activity,
                "verificationUrl", verificationUrl,
                "timeExpire", timeExpire
        ));
    }

    @ApplicationModuleListener
    public void handleUserRegisteredEvent(VerificationSendEmailRegisteredEvent event) {
        log.info("has received the event send verified email");
        sendEmailVerify(event.toEmail(), event.title(), event.fullName(),
                event.activity(), event.verificationUrl(), event.timeExpire());
    }

    @ApplicationModuleListener
    public void deleteVerificationBySessionIds(VerificationSessionIdsRegisteredEvent verificationSessionIdsRegisteredEvent){
        int verificationDeleted = verificationRepository.deleteBySessionIdIn(verificationSessionIdsRegisteredEvent.sessionIds());
        log.info("{} verification deleted by session deleted", verificationDeleted);
    }
}
