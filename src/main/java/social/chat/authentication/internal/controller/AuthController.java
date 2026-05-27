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
import social.chat.authentication.api.dto.FirebaseLoginRequest;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.authentication.internal.service.AuthService;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.dto.Response;

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

    @PostMapping("login/oauth2")
    public ResponseEntity<Response<?>> loginGoogle(@Valid @RequestBody FirebaseLoginRequest firebaseLoginRequest,
                                                   @CookieValue(name = GlobalParamName.DEVICE_ID_COOKIE_NAME)
                                                   Long deviceId) {
        var response = authService.oauth2Login(firebaseLoginRequest, deviceId, null, null,
                null, null, null);
        ResponseCookie cookieDevice = authenticationImp.getResponseCookie(GlobalParamName.DEVICE_ID_COOKIE_NAME,
                response.getData().getDeviceId(), Duration.ofDays(3650));
        ResponseCookie cookieToken = authenticationImp.getResponseCookie(GlobalParamName.REFRESH_TOKEN_COOKIE_NAME,
                response.getData().getRefreshToken(), Duration.ofSeconds(jwtProperties.getRefreshTokenExpire()));
        response.getData().setRefreshToken(null);
        response.getData().setDeviceId(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookieDevice.toString())
                .header(HttpHeaders.SET_COOKIE, cookieToken.toString())
                .body(response);
    }

    @GetMapping("refresh-token")
    public ResponseEntity<Response<?>> refreshToken(@CookieValue(name = GlobalParamName.REFRESH_TOKEN_COOKIE_NAME)
                                                    String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
