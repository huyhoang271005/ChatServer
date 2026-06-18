package social.chat.authentication.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import social.chat.authentication.internal.AuthenticationMessage;

public record EmailRequest (
    @NotBlank(message = AuthenticationMessage.Validation.EMAIL_NOT_BLANK)
    @Email(message = AuthenticationMessage.Validation.EMAIL_INVALID)
    String emailName
){}
