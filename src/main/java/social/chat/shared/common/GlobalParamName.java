package social.chat.shared.common;

public final class GlobalParamName {
    public static final class Cookie {
        public static final String REFRESH_TOKEN = "refresh_token";
        public static final String DEVICE_ID = "device_id";
    }

    public static final class Attribute {
        public static final String DEVICE_NAME = "device_name";
        public static final String DEVICE_TYPE = "device_type";
        public static final String LOCATION =  "location";
        public static final String IP_ADDRESS = "ip_address";
    }

    public static final class Jwt {
        public static final String SESSION_ID = "sessionId";
    }

    public static final class CacheName {
        public static final String SESSION = "session";
        public static final String USER_FCM = "user-fcm";
        public static final String USER_SHORT_PROFILE = "user-short-profile";
        public static final String USER = "user";
        public static final String ROLE = "role";
        public static final String CONVERSATION = "conversation";
        public static final String MESSAGE = "message";
        public static final String REACTION = "reaction";
        public static final String USER_PRESENCE = "user-presence";
    }

}
