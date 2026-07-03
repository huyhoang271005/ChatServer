package social.chat.conversation.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MemberDto (
        @NotNull(message = "conversationId not be null")
        Long conversationId,
        @NotEmpty(message = "List user id not be empty")
        List<Long> userIds
){}
