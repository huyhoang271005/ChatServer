package social.chat.profile.api.dto;

import java.time.Instant;

public record EmailDto (
    Long emailId,
    Long userId,
    String emailName,
    Boolean verified,
    Instant createdAt
){}
