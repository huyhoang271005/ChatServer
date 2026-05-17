package social.chat.authentication.internal.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.dto.UserDto;
import social.chat.authentication.internal.service.AuthService;
import social.chat.config.common.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth")
public class AuthController {
    AuthService authService;

    @PostMapping("send-verified-email")
    public ResponseEntity<Response<?>> sendVerificationEmail(@RequestBody UserDto userDto) {
        return ResponseEntity.ok(authService.sendVerificationEmail(userDto.getEmail()));
    }
}
