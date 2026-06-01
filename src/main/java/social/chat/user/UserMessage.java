package social.chat.user;

public final class UserMessage {
    public static final class User {
        public static final String EXITS = "user.exists";
        public static final String NOT_EXITS = "user.not-exists";
        public static final String NOT_VERIFIED = "user.not-verified";
    }

    public static final class Account {
        public static final String LOCKED = "account.locked";
        public static final String INACTIVE = "account.inactive";
        public static final String INVALID = "account.invalid";
        public static final String BANNED = "account.banned";
        public static final String TIME_BANNED_INVALID = "account.time-banned-invalid";
    }
}
