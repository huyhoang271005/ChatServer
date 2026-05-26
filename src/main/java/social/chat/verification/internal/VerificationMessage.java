package social.chat.verification.internal;

public final class VerificationMessage {
    public static final class Verification {
        public static final String NOT_EXISTS = "verification.not-exists";
        public static final String EXPIRED = "verification.expired";
        public static final String INVALID = "verification.invalid";
        public static final String USED = "verification.used";
        public static final String SUCCESS = "verification.success";
        public static final String EMAIL_VERIFIED = "verification.email.verified";
        public static final String EMAIL_VERIFICATION = "verification.email";
        public static final String DEVICE_VERIFICATION = "verification.device";
        public static final String RESET_PASSWORD_VERIFICATION = "verification.reset-password";
    }

    public static final class Validation {
        public static final String PASSWORD_INVALID = "validation.password.invalid";
    }

    public static final class EmailSender {
        public static final String SUCCESS = "email.send";
    }

    public static final class Session {
        public static final String VERIFIED = "session.verified";
    }

    public static final String SECURITY = "security";
}
