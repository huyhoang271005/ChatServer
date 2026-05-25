package social.chat.user;

public final class UserMessage {
    public static final class User {
        public static final String EXITS = "user.exists";
        public static final String NOT_EXITS = "user.not-exists";
        public static final String NOT_VERIFIED = "user.not-verified";
    }

    public static final class Account {
        public static final String BLOCKED = "account.blocked";
        public static final String INACTIVE = "account.inactive";
        public static final String INVALID = "account.invalid";
    }
}
