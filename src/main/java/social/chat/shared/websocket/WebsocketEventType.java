package social.chat.shared.websocket;

public enum WebsocketEventType {
    USER_ONLINE,
    USER_OFFLINE,
    UPDATE_CONVERSATION,
    DELETE_CONVERSATION,
    NEW_MESSAGE,
    UPDATE_MESSAGE,
    TYPING,
    UNTYPING,
    SEEN_MESSAGE,
    REVOKE_MESSAGE,
    ERROR
}
