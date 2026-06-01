package social.chat.verification.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.verification.internal.enums.VerificationStatus;
import social.chat.verification.internal.enums.VerificationType;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerificationResponse {
    VerificationType verificationType;
    Instant usedAt;
    VerificationStatus verificationStatus;
    Instant createdAt;
}
