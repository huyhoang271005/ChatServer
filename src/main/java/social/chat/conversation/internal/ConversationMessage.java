package social.chat.conversation.internal;

public final class ConversationMessage {
    public static final String NOT_EXISTS = "conversation.not-exists";
    public static final String EXISTS = "conversation.exists";
    public static final String USER_NOT_IN = "conversation.user-not-in";
    public static final String FORBIDDEN = "conversation.forbidden";

    public static final class Member {
        public static final String EXISTS = "conversation.member.exists";
        public static final String NOT_EXISTS = "conversation.member.not-exists";
        public static final String TOO_LOW = "conversation.member.too-low";
        public static final String CANT_REMOVE = "conversation.member.cannot-remove";
    }

}
