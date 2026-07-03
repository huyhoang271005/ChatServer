package social.chat.profile.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.NonNull;
import org.springframework.modulith.NamedInterface;
import social.chat.profile.internal.ProfileMessage;
import social.chat.profile.internal.enums.Gender;

import java.time.Instant;
import java.time.LocalDate;

@NamedInterface
public record ProfileDto (
    Long userId,
    @NotBlank
    @Pattern(regexp = ProfileRegexValidation.USERNAME, message = ProfileMessage.Validation.USERNAME_INVALID)
    String username,
    @Pattern(regexp = ProfileRegexValidation.FULL_NAME, message = ProfileMessage.Validation.FULL_NAME_INVALID)
    String fullName,
    String avatarUrl,
    @NonNull
    @Past
    LocalDate birthday,
    @NonNull
    Gender gender,
    Instant updatedAt
){}
