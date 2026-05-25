package social.chat.profile.internal.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.JwtProperties;
import social.chat.config.common.GlobalParamName;
import social.chat.profile.api.dto.FullNameRequest;
import social.chat.profile.api.dto.ProfileDto;
import social.chat.profile.internal.service.ProfileService;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("profiles")
public class ProfileController {
    ProfileService profileService;
    JwtProperties  jwtProperties;

    @PostMapping("auth/{userId}")
    public ResponseEntity<?> createProfile(@PathVariable Long userId,
                                           @Valid @RequestBody FullNameRequest fullNameRequest,
                                           @CookieValue(name = GlobalParamName.DEVICE_ID_COOKIE_NAME)
                                           Long deviceId) {
        var response = profileService.createProfile(userId, fullNameRequest.getFullName(), deviceId);
        ResponseCookie cookie = ResponseCookie.from(GlobalParamName.REFRESH_TOKEN_COOKIE_NAME,
                        response.getData().getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("None")
                .maxAge(Duration.ofDays(jwtProperties.getRefreshTokenExpire()))
                .build();
        response.getData().setRefreshToken(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@Valid @RequestBody ProfileDto profileDto,
                                           @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(profileService.updateProfile(Long.parseLong(jwt.getSubject()), profileDto));
    }
}
