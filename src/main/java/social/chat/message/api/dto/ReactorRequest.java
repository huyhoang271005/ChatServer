package social.chat.message.api.dto;

import jakarta.validation.constraints.NotNull;
import social.chat.message.internal.ReactionType;

public record ReactorRequest(
        @NotNull(message = "messageId not be null")
        Long messageId,
        ReactionType reactionType
) {
}
