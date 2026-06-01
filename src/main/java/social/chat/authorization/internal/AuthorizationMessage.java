package social.chat.authorization.internal;

public final class AuthorizationMessage {
    public static final class Role {
        public static final String EXISTS = "role.exists";
        public static final String NOT_EXISTS = "role.not-exists";
        public static final String DEFAULT_CANT_REMOVE = "role.default.cant-remove";
        public static final String DEFAULT_CAN_UPDATE = "role.default.can-update";
        public static final String DELETED = "role.deleted";
    }
}
