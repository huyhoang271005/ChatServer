package social.chat.authentication.api.dto;

public record TokenDto (
    Long userId,
    Long deviceId,
    Boolean verifiedEmail,
    Boolean verifiedDevice,
    String accessToken,
    String refreshToken,
    boolean hasProfile,
    boolean updateProfile
){}
