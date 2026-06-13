package social.chat.user.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import social.chat.user.internal.AccountStatus;

import java.time.Instant;

@NamedInterface
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendUser {
    Long userId;
    AccountStatus accountStatus;
    Long roleId;
    Instant expireAt;
}
