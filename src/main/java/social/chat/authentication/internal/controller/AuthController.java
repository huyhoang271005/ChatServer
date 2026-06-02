package social.chat.authentication.internal.controller;

import jakarta.servlet.http.HttpServletRequest;
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
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.JwtProperties;
import social.chat.authentication.api.dto.FirebaseLoginRequest;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.authentication.internal.service.AuthenticationService;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.dto.Response;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("auth")
public class AuthController {
    AuthenticationService authenticationService;
    AuthenticationImp authenticationImp;
    JwtProperties jwtProperties;

    @PostMapping("login")
    public ResponseEntity<Response<?>> login(@Valid @RequestBody LoginRequest loginRequest,
                                             @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID,
                                             required = false) Long deviceId) {
        var response = authenticationService.login(loginRequest, deviceId);
        ResponseCookie cookie = authenticationImp.getResponseCookie(GlobalParamName.Cookie.REFRESH_TOKEN,
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
                                                   @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID)
                                                   Long deviceId,
                                                   @Header(name = HttpHeaders.USER_AGENT) String userAgent,
                                                   HttpServletRequest request) {
        var response = authenticationService.oauth2Login(firebaseLoginRequest,
                deviceId, String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_NAME)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_TYPE)),
                userAgent,
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.IP_ADDRESS)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.LOCATION)));
        ResponseCookie cookieDevice = authenticationImp.getResponseCookie(GlobalParamName.Cookie.DEVICE_ID,
                response.getData().getDeviceId(), Duration.ofDays(3650));
        ResponseCookie cookieToken = authenticationImp.getResponseCookie(GlobalParamName.Cookie.REFRESH_TOKEN,
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
    public ResponseEntity<Response<?>> refreshToken(@CookieValue(name = GlobalParamName.Cookie.REFRESH_TOKEN)
                                                    String refreshToken,
                                                    @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID)
                                                    Long deviceId, HttpServletRequest request) {
        var response =  authenticationService.refreshToken(refreshToken, deviceId,
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.IP_ADDRESS)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.LOCATION)));
        response.getData().setRefreshToken(null);
        return ResponseEntity.ok(response);
    }
}
