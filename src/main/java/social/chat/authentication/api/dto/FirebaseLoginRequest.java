package social.chat.authentication.api.dto;

import jakarta.validation.constraints.NotBlank;

public record FirebaseLoginRequest (
    @NotBlank
    String firebaseToken,
    String fcmToken
){}
