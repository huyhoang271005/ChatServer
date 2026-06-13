package social.chat.authentication.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SessionDto {
    Long sessionId;
    String location;
    Boolean validated;
    Boolean revoked;
    String ipAddress;
    Instant lastLogin;
    Instant createdAt;
    DeviceDto device;
    boolean mySession;
}
