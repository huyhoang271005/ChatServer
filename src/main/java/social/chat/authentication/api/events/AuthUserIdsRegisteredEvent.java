package social.chat.authentication.api.events;

import org.springframework.modulith.NamedInterface;

import java.util.List;

@NamedInterface
public record AuthUserIdsRegisteredEvent(
        List<Long> userIds
) {
}
