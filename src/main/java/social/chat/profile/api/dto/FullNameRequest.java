package social.chat.profile.api.dto;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.profile.internal.ProfileMessage;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FullNameRequest {
    @Pattern(regexp = ProfileRegexValidation.FULL_NAME, message = ProfileMessage.Validation.FULL_NAME_INVALID)
    String fullName;
}
