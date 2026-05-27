package social.chat.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenDto {
    String userId;
    String deviceId;
    Boolean verifiedEmail;
    Boolean verifiedDevice;
    String accessToken;
    String refreshToken;
    boolean hasProfile;
    boolean updateProfile;
}
