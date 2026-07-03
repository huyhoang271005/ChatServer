package social.chat.user_presence.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("user-presences")
public class UserPresenceController {
    UserPresenceCache userPresenceCache;

    @PutMapping
    public ResponseEntity<Response<?>> getUserPresences(@RequestBody Set<Long> userIds) {
        return ResponseEntity.ok(Response.success(
                GlobalMessage.Success.GET,
                userPresenceCache.getPresencesCache(userIds)
                        .orElse(List.of())
        ));
    }
}
