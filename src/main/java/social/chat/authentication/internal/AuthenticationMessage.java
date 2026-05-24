package social.chat.authentication.internal;

public final class AuthenticationMessage {
    public static final class User {
        public static final String EXITS = "user.exists";
        public static final String NOT_EXITS = "user.not-exists";
        public static final String NOT_VERIFIED = "user.not-verified";
    }

    public static final class EmailSender {
        public static final String SUCCESS = "email.send";
    }

    public static final class Account {
        public static final String BLOCKED = "account.blocked";
        public static final String INACTIVE = "account.inactive";
        public static final String INVALID = "account.invalid";
    }

    public static final class Role {
        public static final String PENDING_PROFILE = "role.pending-profile";
        public static final String EXISTS = "role.exists";
        public static final String NOT_EXISTS = "role.not-exists";
        public static final String PERMISSION_INVALID = "role.permission-invalid";
        public static final String PERMISSION_EXISTS = "role.permission-exists";
        public static final String PERMISSION_NOT_EXISTS = "role.permission-not-exists";
    }

    public static final class Verification {
        public static final String NOT_EXISTS = "verification.not-exists";
        public static final String EXPIRED = "verification.expired";
        public static final String INVALID = "verification.invalid";
        public static final String SUCCESS = "verification.success";
        public static final String EMAIL_VERIFIED = "verification.email.verified";
        public static final String DEVICE_VERIFIED = "verification.device.verified";
        public static final String EMAIL_VERIFICATION = "verification.email";
        public static final String DEVICE_VERIFICATION = "verification.device";
        public static final String RESET_PASSWORD_VERIFICATION = "verification.reset-password";
    }

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

    public static final String SECURITY = "security";
}
