package social.chat.authentication.internal.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.internal.service.SessionService;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.dto.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("sessions")
public class SessionController {
    SessionService sessionService;

    @GetMapping
    public ResponseEntity<Response<?>> getSessions(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam(required = false) Long lastId,
                                                   Pageable pageable) {
        return ResponseEntity.ok(sessionService.getSessions(Long.parseLong(jwt.getSubject()),
                lastId, pageable, jwt.getClaim(GlobalParamName.Jwt.SESSION_ID)));
    }

    @DeleteMapping("{sessionId}")
    public ResponseEntity<Response<?>> deleteSession(@PathVariable Long sessionId,
                                                     @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(sessionService.deleteSession(Long.parseLong(jwt.getSubject()),
                sessionId));
    }
}
