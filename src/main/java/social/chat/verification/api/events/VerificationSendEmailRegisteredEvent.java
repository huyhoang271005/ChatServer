package social.chat.verification.api.events;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public record VerificationSendEmailRegisteredEvent(
        String toEmail,
        String title,
        String fullName,
        String activity,
        String verificationUrl,
        String timeExpire
){}
