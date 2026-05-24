package social.chat.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import social.chat.authentication.internal.AuthenticationMessage;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NamedInterface
public class LoginRequest extends EmailRequest {
    @Pattern(regexp = AuthRegexValidation.PASSWORD, message = AuthenticationMessage.Validation.PASSWORD_INVALID)
    String password;
}
