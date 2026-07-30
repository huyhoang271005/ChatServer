package social.chat.authentication.internal.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.JwtProperties;
import social.chat.authentication.api.dto.FirebaseLoginRequest;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.mapper.SessionMapper;
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
    SessionMapper sessionMapper;

    private ResponseEntity<Response<?>> createCookie(Response<TokenDto> response) {
        String cookieDevice = response.data().deviceId() != null ?
                authenticationImp.getResponseCookie(GlobalParamName.Cookie.DEVICE_ID,
                        response.data().deviceId().toString(), Duration.ofDays(3650))
                .toString(): null;
        String cookieToken = response.data().refreshToken() != null ?
                authenticationImp.getResponseCookie(GlobalParamName.Cookie.REFRESH_TOKEN,
                        response.data().refreshToken(), Duration.ofSeconds(jwtProperties.getRefreshTokenExpire()))
                .toString() : null;
        return ResponseEntity
                .status(HttpStatus.OK)
                .headers(httpHeaders -> {
                    if(cookieToken != null) {
                        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieToken);
                    }
                    if(cookieDevice != null) {
                        httpHeaders.add(HttpHeaders.SET_COOKIE, cookieDevice);
                    }
                })
                .body(new Response<>(response.success(), response.message(), sessionMapper
                        .toTokenDto(response.data())));
    }

    @PostMapping("login")
    public ResponseEntity<Response<?>> login(@Valid @RequestBody LoginRequest loginRequest,
                                             @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID,
                                             required = false) Long deviceId) {
        var response = authenticationService.login(loginRequest, deviceId);
        return createCookie(response);
    }

    @PostMapping("login/oauth2")
    public ResponseEntity<Response<?>> loginGoogle(@Valid @RequestBody FirebaseLoginRequest firebaseLoginRequest,
                                                   @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID , required = false)
                                                   Long deviceId,
                                                   @RequestHeader(name = HttpHeaders.USER_AGENT) String userAgent,
                                                   HttpServletRequest request) {
        var response = authenticationService.oauth2Login(firebaseLoginRequest,
                deviceId, String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_NAME)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_TYPE)),
                userAgent,
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.IP_ADDRESS)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.LOCATION)));
        return createCookie(response);
    }

    @PostMapping("refresh-token")
    public ResponseEntity<Response<?>> refreshToken(@CookieValue(name = GlobalParamName.Cookie.REFRESH_TOKEN)
                                                    String refreshToken,
                                                    @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID)
                                                    Long deviceId, HttpServletRequest request) {
        var response =  authenticationService.refreshToken(refreshToken, deviceId,
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.IP_ADDRESS)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.LOCATION)));
        return createCookie(response);
    }
}
