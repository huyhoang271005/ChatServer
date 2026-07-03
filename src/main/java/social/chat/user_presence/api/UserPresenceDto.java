package social.chat.user_presence.api;

import com.fasterxml.jackson.annotation.JsonIgnore;
import social.chat.user_presence.internal.UserPresenceStatus;

import java.time.Instant;

public record UserPresenceDto (
        Long userId,
        UserPresenceStatus userPresenceStatus,
        Instant lastOnline,
        @JsonIgnore
        Integer count,
        @JsonIgnore
        boolean isNew
){}
