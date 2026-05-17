package social.chat.authentication.api.events;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public record AuthRegisteredEvent (
        String toEmail,
        String title,
        String fullName,
        String activity,
        String verificationUrl,
        String timeExpire
){}
