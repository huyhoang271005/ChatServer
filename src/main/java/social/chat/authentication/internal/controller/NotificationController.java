package social.chat.authentication.internal.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.dto.FirebaseLoginRequest;
import social.chat.authentication.internal.service.NotificationService;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.dto.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("notifications")
public class NotificationController {
    NotificationService notificationService;

    @GetMapping("status")
    public ResponseEntity<Response<?>> notificationStatus(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(notificationService.notificationStatus(
                jwt.getClaim(GlobalParamName.Jwt.SESSION_ID)));
    }

    @PostMapping("enable")
    public ResponseEntity<Response<?>> enable(@AuthenticationPrincipal Jwt jwt,
                                              @RequestBody FirebaseLoginRequest firebaseLoginRequest) {
        return ResponseEntity.ok(notificationService.enableNotification(
                jwt.getClaim(GlobalParamName.Jwt.SESSION_ID),
                firebaseLoginRequest,
                true
        ));
    }

    @PostMapping("disable")
    public ResponseEntity<Response<?>> disable(@AuthenticationPrincipal Jwt jwt,
                                              @RequestBody FirebaseLoginRequest firebaseLoginRequest) {
        return ResponseEntity.ok(notificationService.enableNotification(
                jwt.getClaim(GlobalParamName.Jwt.SESSION_ID),
                firebaseLoginRequest,
                false
        ));
    }
}
