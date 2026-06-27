package social.chat.authentication.api.dto;

public record UserIdWithFcmToken(
        Long userId,
        String fcmToken
) {
}
