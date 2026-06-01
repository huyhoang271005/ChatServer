package social.chat.user.internal;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import social.chat.authentication.api.dto.LoginRequest;
import social.chat.shared.dto.Response;
import social.chat.user.api.dto.ExtendUser;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("users")
public class UserController {
    UserService userService;

    @PostMapping("auth")
    public ResponseEntity<Response<?>> createUser(@Valid @RequestBody LoginRequest loginRequest) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.createUserWithEmail(loginRequest));
    }

    @DeleteMapping("{userId}")
    public ResponseEntity<Response<?>> deleteUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.SoftDeleteUser(userId));
    }

    @GetMapping("extend")
    public ResponseEntity<Response<?>> getMyExtendUser(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.getExtendedUser(Long.parseLong(jwt.getSubject())));
    }

    @GetMapping("extend/{userId}")
    public ResponseEntity<Response<?>> getExtendedUser(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getExtendedUser(userId));
    }

    @PreAuthorize("hasAuthority('UPDATE_EXTEND_USER')")
    @PatchMapping
    public ResponseEntity<Response<?>> updateExtendUser(@RequestBody ExtendUser extendUser) {
        return ResponseEntity.ok(userService.updateExtendedUser(extendUser));
    }
}
