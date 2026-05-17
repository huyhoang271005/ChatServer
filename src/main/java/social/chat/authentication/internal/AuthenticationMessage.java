package social.chat.authentication.internal;

public final class AuthenticationMessage {
    public static final class User {
        public static final String EXITS = "user.error.exists";
        public static final String NOT_EXITS = "user.error.not-exists";
        public static final String NOT_VERIFIED = "user.error.not-verified";
    }

    public static final class EmailSender {
        public static final String SUCCESS = "email.success.send";
        public static final String VERIFIED_EMAIL = "email.verified";
        public static final String EMAIL_VERIFIED = "email.error.verified";
    }

    public static final String SECURITY = "security";
}
