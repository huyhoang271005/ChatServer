package social.chat.verification.api.dto;

import jakarta.validation.constraints.Pattern;
import org.springframework.modulith.NamedInterface;
import social.chat.authentication.api.dto.AuthRegexValidation;
import social.chat.verification.internal.VerificationMessage;

@NamedInterface
public record VerificationRequest (
    Long verificationId,
    Long deviceId,
    @Pattern(regexp = AuthRegexValidation.PASSWORD, message = VerificationMessage.Validation.PASSWORD_INVALID)
    String newPassword
){}
