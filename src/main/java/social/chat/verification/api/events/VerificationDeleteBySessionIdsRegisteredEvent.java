package social.chat.verification.api.events;

import java.util.List;

public record VerificationDeleteBySessionIdsRegisteredEvent(
        List<Long> sessionIds
) {
}
