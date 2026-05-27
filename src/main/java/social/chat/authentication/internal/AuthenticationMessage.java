package social.chat.authentication.internal;

public final class AuthenticationMessage {
    public static final class Validation {
        public static final String EMAIL_NOT_BLANK = "validation.email.not-blank";
        public static final String EMAIL_INVALID = "validation.email.invalid";
        public static final String PASSWORD_INVALID = "validation.password.invalid";
        public static final String PASSWORD_INCORRECT = "validation.password.incorrect";
    }

    public static final class Session {
        public static final String NOT_EXISTS = "session.not-exists";
        public static final String EXPIRED = "session.expired";
    }

    public static final class Oauth2 {
        public static final String NOT_FOUND_EMAIL = "oauth2.email.not-found";
    }
}
