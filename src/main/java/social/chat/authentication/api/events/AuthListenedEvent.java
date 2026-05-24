package social.chat.authentication.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import social.chat.config.emailSender.EmailSenderService;
import social.chat.config.common.ApplicationProperties;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthListenedEvent {
    EmailSenderService emailSenderService;
    ApplicationProperties applicationProperties;
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

    @EventListener
    public void handleUserRegisteredEvent(AuthRegisteredEvent event) {
        log.info("has received the event send verified email");
        sendEmailVerify(event.toEmail(), event.title(), event.fullName(),
                event.activity(), event.verificationUrl(), event.timeExpire());
    }
}
