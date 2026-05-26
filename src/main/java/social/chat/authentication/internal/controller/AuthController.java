package social.chat.authentication.internal.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.JwtProperties;
import social.chat.user.api.dto.LoginRequest;
import social.chat.authentication.internal.service.AuthService;
import social.chat.config.common.GlobalParamName;
import social.chat.config.common.Response;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth")
public class AuthController {
    AuthService authService;
    AuthenticationImp authenticationImp;
    JwtProperties jwtProperties;

    @PostMapping("login")
    public ResponseEntity<Response<?>> login(@Valid @RequestBody LoginRequest loginRequest,
                                             @CookieValue(name = GlobalParamName.DEVICE_ID_COOKIE_NAME,
                                             required = false) Long deviceId) {
        var response = authService.login(loginRequest, deviceId);
        ResponseCookie cookie = authenticationImp.getResponseCookie(GlobalParamName.REFRESH_TOKEN_COOKIE_NAME,
                response.getData().getRefreshToken(),
                Duration.ofSeconds(jwtProperties.getRefreshTokenExpire()));
        response.getData().setRefreshToken(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @GetMapping("refresh-token")
    public ResponseEntity<Response<?>> refreshToken(@CookieValue(name = GlobalParamName.REFRESH_TOKEN_COOKIE_NAME)
                                                    String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
