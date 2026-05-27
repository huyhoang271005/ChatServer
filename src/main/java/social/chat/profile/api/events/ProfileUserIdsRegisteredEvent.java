package social.chat.profile.api.events;

import org.springframework.modulith.NamedInterface;

import java.util.List;

@NamedInterface
public record ProfileUserIdsRegisteredEvent(
        List<Long> userIds
) {}
