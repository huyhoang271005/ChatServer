package social.chat.verification.internal;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.AuthenticationImp;
import social.chat.authentication.api.dto.EmailRequest;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.dto.Response;
import social.chat.verification.api.dto.VerificationRequest;

import java.time.Duration;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("verifications")
public class VerificationController {
    VerificationService verificationService;
    AuthenticationImp authenticationImp;

    @PostMapping("send-verification-email")
    public ResponseEntity<Response<?>> sendVerificationEmail(@Valid @RequestBody EmailRequest emailRequest,
                                                             @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID,
                                                                     required = false)
                                                             Long deviceId,
                                                             @Header(name = HttpHeaders.USER_AGENT) String userAgent,
                                                             HttpServletRequest request) {
        var response = verificationService.sendVerificationEmail(emailRequest.getEmailName(),
                deviceId, String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_NAME)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_TYPE)),
                userAgent,
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.IP_ADDRESS)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.LOCATION)));
        var cookie = authenticationImp.getResponseCookie(GlobalParamName.Cookie.DEVICE_ID,
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
                                                                     @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID,
                                                                             required = false)
                                                                     Long deviceId,
                                                                     @Header(name = HttpHeaders.USER_AGENT)
                                                                         String userAgent,
                                                                     HttpServletRequest request) {
        var response = verificationService.sendVerificationChangePassword(emailRequest.getEmailName(),
                deviceId,
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_NAME)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_TYPE)),
                userAgent,
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.IP_ADDRESS)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.LOCATION)));
        var cookie = authenticationImp.getResponseCookie(GlobalParamName.Cookie.DEVICE_ID,
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
                                                              @CookieValue(name = GlobalParamName.Cookie.DEVICE_ID,
                                                                      required = false)
                                                              Long deviceId,
                                                              @Header(name = HttpHeaders.USER_AGENT)
                                                                  String userAgent,
                                                              HttpServletRequest request) {
        var response = verificationService.sendVerificationDevice(emailRequest.getEmailName(),
                deviceId, String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_NAME)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.DEVICE_TYPE)),
                userAgent,
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.IP_ADDRESS)),
                String.valueOf(request.getAttribute(GlobalParamName.Attribute.LOCATION)));
        var cookie = authenticationImp.getResponseCookie(GlobalParamName.Cookie.DEVICE_ID,
                String.valueOf(response.getData()),
                Duration.ofDays(3650));
        response.setData(null);
        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, cookie.toString())
                .body(response);
    }

    @PostMapping("verify")
    public ResponseEntity<Response<?>> verifyUser(@Valid @RequestBody VerificationRequest verificationRequest) {
        return ResponseEntity.ok(verificationService.verify(verificationRequest));
    }

    @GetMapping("session/{sessionId}")
    public ResponseEntity<Response<?>> getVerificationBySessionId(@PathVariable Long sessionId) {
        return ResponseEntity.ok(verificationService.getVerifications(sessionId));
    }
}
