package social.chat.profile.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.profile.internal.ProfileMessage;
import social.chat.profile.internal.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDto extends FullNameRequest{
    String userId;
    @NotBlank
    @Pattern(regexp = ProfileRegexValidation.USERNAME, message = ProfileMessage.Validation.USERNAME_INVALID)
    String username;
    String avatarUrl;
    String avatarId;
    @NonNull
    @Past
    LocalDate birthday;
    @NonNull
    Gender gender;
    Instant updatedAt;
}
