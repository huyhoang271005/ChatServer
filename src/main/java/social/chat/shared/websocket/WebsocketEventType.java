package social.chat.shared.websocket;

public enum WebsocketEventType {
    USER_ONLINE,
    USER_OFFLINE,
    NEW_CONVERSATION,
    UPDATE_CONVERSATION,
    NEW_MESSAGE,
    TYPING,
    UNTYPING,
    SEEN_MESSAGE,
    REVOKE_MESSAGE,
    ADD_MEMBER,
    DELETE_MEMBER
}
