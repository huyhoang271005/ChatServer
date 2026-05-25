package social.chat.user.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import social.chat.authentication.api.dto.AuthRegexValidation;
import social.chat.authentication.internal.AuthenticationMessage;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NamedInterface
public class LoginRequest {
    @NotBlank(message = AuthenticationMessage.Validation.EMAIL_NOT_BLANK)
    @Email(message = AuthenticationMessage.Validation.EMAIL_INVALID)
    String emailName;
    @Pattern(regexp = AuthRegexValidation.PASSWORD, message = AuthenticationMessage.Validation.PASSWORD_INVALID)
    String password;
}
