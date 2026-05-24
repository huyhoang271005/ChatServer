package social.chat.profile.internal;

public final class ProfileMessage {
    public static final class Profile {
        public static final String EXITS = "profile.exists";
        public static final String NOT_EXITS = "profile.not-exists";
    }

    public static final class Email {
        public static final String EXITS = "email.exists";
        public static final String NOT_EXITS = "email.not-exists";
        public static final String VERIFIED = "email.verified";
    }

    public static final class Validation {
        public static final String USERNAME_INVALID = "validation.username.invalid";
        public static final String FULL_NAME_INVALID = "validation.full-name.invalid";
    }
}
