package social.chat.authorization.internal;

public final class AuthorizationMessage {
    public static final class Role {
        public static final String EXISTS = "role.exists";
        public static final String NOT_EXISTS = "role.not-exists";
        public static final String DEFAULT_CANT_REMOVE = "role.default.cant-remove";
        public static final String PERMISSION_INVALID = "role.permission-invalid";
        public static final String PERMISSION_EXISTS = "role.permission-exists";
        public static final String PERMISSION_NOT_EXISTS = "role.permission-not-exists";
    }
}
