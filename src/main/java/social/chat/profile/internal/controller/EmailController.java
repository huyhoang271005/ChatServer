package social.chat.profile.internal.controller;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import social.chat.profile.api.dto.EmailDto;
import social.chat.profile.internal.service.EmailService;
import social.chat.shared.dto.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("emails")
public class EmailController {
    EmailService emailService;

    @GetMapping
    public ResponseEntity<Response<?>> getMyEmails(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(emailService.getEmails(Long.parseLong(jwt.getSubject())));
    }

    @GetMapping("{userId}")
    public ResponseEntity<Response<?>> getEmail(@PathVariable Long userId) {
        return  ResponseEntity.ok(emailService.getEmails(userId));
    }

    @PreAuthorize("hasAuthority('ADD_EMAIL')")
    @PostMapping("{userId}")
    public ResponseEntity<Response<?>> createEmail(@Valid @RequestBody EmailDto emailDto,
                                                   @PathVariable Long userId) {
        return ResponseEntity.ok(emailService.createEmail(emailDto, userId));
    }

    @PreAuthorize("hasAuthority('DELETE_EMAIL')")
    @DeleteMapping("{emailId}")
    public ResponseEntity<Response<?>> deleteEmail(@PathVariable Long emailId) {
        return ResponseEntity.ok(emailService.deleteEmail(emailId));
    }
}
