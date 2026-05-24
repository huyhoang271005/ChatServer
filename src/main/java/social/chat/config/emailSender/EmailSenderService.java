package social.chat.config.emailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.modulith.NamedInterface;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@NamedInterface
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailSenderService {
    JavaMailSender mailSender;
    TemplateEngine templateEngine;
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
}
