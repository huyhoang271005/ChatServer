package social.chat.authentication.api.dto;

public record SessionValidation (
    Long sessionId,
    Long userId,
    Long deviceId,
    boolean validated
){}
