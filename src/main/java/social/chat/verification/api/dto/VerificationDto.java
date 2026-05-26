package social.chat.verification.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.authentication.api.dto.AuthRegexValidation;
import social.chat.verification.internal.VerificationMessage;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationDto {
    @NotBlank
    String verificationId;
    Long deviceId;
    @Pattern(regexp = AuthRegexValidation.PASSWORD, message = VerificationMessage.Validation.PASSWORD_INVALID)
    String newPassword;
}
