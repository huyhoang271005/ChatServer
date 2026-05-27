package social.chat.verification.api.events;

import java.util.List;

public record VerificationSessionIdsRegisteredEvent(
        List<Long> sessionIds
) {
}
