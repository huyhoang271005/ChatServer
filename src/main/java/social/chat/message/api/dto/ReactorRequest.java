package social.chat.message.api.dto;

import jakarta.validation.constraints.NotNull;
import social.chat.message.internal.ReactionType;

public record ReactorRequest(
        @NotNull
        Long messageId,
        ReactionType reactionType
) {
}
