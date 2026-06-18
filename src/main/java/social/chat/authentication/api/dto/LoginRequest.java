package social.chat.authentication.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.modulith.NamedInterface;
import social.chat.authentication.internal.AuthenticationMessage;

@NamedInterface
public record LoginRequest (
    @NotBlank(message = AuthenticationMessage.Validation.EMAIL_NOT_BLANK)
    @Email(message = AuthenticationMessage.Validation.EMAIL_INVALID)
    String emailName,
    @Pattern(regexp = AuthRegexValidation.PASSWORD, message = AuthenticationMessage.Validation.PASSWORD_INVALID)
    String password,
    String fcmToken
){}
