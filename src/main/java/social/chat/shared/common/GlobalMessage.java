package social.chat.shared.common;

import lombok.NoArgsConstructor;
import org.springframework.modulith.NamedInterface;

@NamedInterface
@NoArgsConstructor
public final class GlobalMessage {
    public static final class Error {
        public static final String INTERNAL = "global.error.internal";
        public static final String DATA_INVALID = "global.error.data.invalid";
        public static final String TOKEN_INVALID = "global.error.token.invalid";
        public static final String FORBIDDEN = "global.error.forbidden";
    }

    public static final class Success {
        public static final String CREATED = "global.success.created";
        public static final String UPDATED = "global.success.updated";
        public static final String DELETED = "global.success.deleted";
        public static final String GET =  "global.success.get";
    }

    public static final class Time {
        public static final String MINUTE = "global.time.minute";
        public static final String HOUR = "global.time.hour";
    }

    public static final class RateLimit {
        public static final String MINUTE = "global.rate-limit.minute";
        public static final String HOUR = "global.rate-limit.hour";
        public static final String SECOND = "global.rate-limit.second";
    }
}
