package social.chat.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import social.chat.authentication.internal.AuthenticationMessage;

@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
@NamedInterface
public class EmailRequest {
    @NotBlank(message = AuthenticationMessage.Validation.EMAIL_NOT_BLANK)
    @Email(message = AuthenticationMessage.Validation.EMAIL_INVALID)
    String emailName;
}
