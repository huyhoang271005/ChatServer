package social.chat.profile.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProfileDto {
    String userId;
    String username;
    String fullName;
    String avatarUrl;
    String avatarId;
    LocalDate birthday;
    Gender gender;
    Instant updatedAt;
}
