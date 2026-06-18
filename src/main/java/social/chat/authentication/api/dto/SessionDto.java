package social.chat.authentication.api.dto;

import java.time.Instant;

public record SessionDto (
    Long sessionId,
    String location,
    Boolean validated,
    Boolean revoked,
    String ipAddress,
    Instant lastLogin,
    Instant createdAt,
    DeviceDto device,
    boolean mySession
) {}
