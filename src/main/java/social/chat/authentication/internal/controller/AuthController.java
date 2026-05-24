package social.chat.authentication.internal.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.dto.EmailRequest;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.authentication.api.dto.VerificationDto;
import social.chat.authentication.internal.service.AuthService;
import social.chat.authentication.internal.service.VerificationService;
import social.chat.config.common.GlobalParamName;
import social.chat.config.common.Response;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth")
public class AuthController {
    AuthService authService;
    VerificationService verificationService;

    private ResponseCookie responseCookie (String paramName, String paramValue, Duration duration) {
        return ResponseCookie.from(paramName, paramValue)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(duration) // 10 year
                .build();
    }

    @PostMapping("login")
    public ResponseEntity<Response<?>> login(@Valid @RequestBody LoginRequest loginRequest,
                                             @CookieValue(name = GlobalParamName.DEVICE_ID_COOKIE_NAME,
                                             required = false) Long deviceId) {
        var response = authService.login(loginRequest, deviceId);
        ResponseCookie cookie = responseCookie(GlobalParamName.REFRESH_TOKEN_COOKIE_NAME, response.getData().getRefreshToken(),
                Duration.ofDays(7));
        response.getData().setRefreshToken(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }
    @PostMapping("send-verification-email")
    public ResponseEntity<Response<?>> sendVerificationEmail(@Valid @RequestBody EmailRequest emailRequest,
                                                             @CookieValue(name = GlobalParamName.DEVICE_ID_COOKIE_NAME,
                                                             required = false)
                                                             Long deviceId,
                                                             @Header(name = HttpHeaders.USER_AGENT) String userAgent) {
        var response = verificationService.sendVerificationEmail(emailRequest.getEmailName(),
                deviceId, null, null, userAgent, null, null);
        var cookie = responseCookie(GlobalParamName.DEVICE_ID_COOKIE_NAME,
                String.valueOf(response.getData()),
                Duration.ofDays(3650));
        response.setData(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("send-verification-reset-password")
    public ResponseEntity<Response<?>> sendVerificationResetPassword(@Valid @RequestBody EmailRequest emailRequest,
                                                             @CookieValue(name = GlobalParamName.DEVICE_ID_COOKIE_NAME,
                                                                     required = false)
                                                             Long deviceId,
                                                             @Header(name = HttpHeaders.USER_AGENT) String userAgent) {
        var response = verificationService.sendVerificationChangePassword(emailRequest.getEmailName(),
                deviceId, null, null, userAgent, null, null);
        var cookie = responseCookie(GlobalParamName.DEVICE_ID_COOKIE_NAME,
                String.valueOf(response.getData()),
                Duration.ofDays(3650));
        response.setData(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("send-verification-device")
    public ResponseEntity<Response<?>> sendVerificationDevice(@Valid @RequestBody EmailRequest emailRequest,
                                                              @CookieValue(name = GlobalParamName.DEVICE_ID_COOKIE_NAME,
                                                              required = false)
                                                             Long deviceId,
                                                              @Header(name = HttpHeaders.USER_AGENT) String userAgent) {
        var response = verificationService.sendVerificationDevice(emailRequest.getEmailName(),
                deviceId, null, null, userAgent, null, null);
        var cookie = responseCookie(GlobalParamName.DEVICE_ID_COOKIE_NAME,
                String.valueOf(response.getData()),
                Duration.ofDays(3650));
        response.setData(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("verify")
    public ResponseEntity<Response<?>> verifyUser(@Valid @RequestBody VerificationDto verificationDto) {
        return ResponseEntity.ok(verificationService.verify(verificationDto));
    }

    @GetMapping("refresh-token")
    public ResponseEntity<Response<?>> refreshToken(@CookieValue(name = GlobalParamName.REFRESH_TOKEN_COOKIE_NAME)
                                                    String refreshToken) {
        return ResponseEntity.ok(authService.refreshToken(refreshToken));
    }
}
