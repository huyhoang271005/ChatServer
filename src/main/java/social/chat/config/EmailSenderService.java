package social.chat.config;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import social.chat.authentication.api.events.AuthRegisteredEvent;
import social.chat.config.common.ApplicationProperties;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@EnableAsync
public class EmailSenderService {
    JavaMailSender mailSender;
    TemplateEngine templateEngine;
    ApplicationProperties applicationProperties;
    @NonFinal
    @Value("${spring.mail.username}")
    String mailUsername;
    @NonFinal
    @Value("${spring.mail.from-name}")
    String fromName;

    @Async
    public void sendEmail(String toEmail, String title, String templateName, Map<String, String> values) {
        try {
            Context context = new Context();
            values.forEach(context::setVariable);
            String emailContent = templateEngine.process(templateName, context);
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true,  "UTF-8");
            helper.setFrom(mailUsername, fromName);
            helper.setTo(toEmail);
            helper.setSubject(title);
            helper.setText(emailContent, true);
            mailSender.send(mimeMessage);
            log.info("Sent email to {}", toEmail);
        } catch (MessagingException e) {
            // Lỗi liên quan đến nội dung, format mail hoặc cấu hình SMTP
            log.error("SMTP/Messaging error when sending to {}: {}", toEmail, e.getMessage());
        } catch (MailException e) {
            // Lỗi kết nối server (Timeout, Connection refused)
            log.error("Network/Connection error to Mail Server when sending to {}: {}", toEmail, e.getMessage());
        } catch (Exception e) {
            // Các lỗi không xác định khác (Thymeleaf render lỗi...)
            log.error("Unexpected error during email processing for {}: {}", toEmail, e.getMessage());
        }
    }

    public void sendEmailVerify(String toEmail, String title, String fullName, String activity,
                                String verificationUrl, String timeExpire) {
        sendEmail(toEmail, title, "verified", Map.of(
                "appName", applicationProperties.getAppName(),
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
