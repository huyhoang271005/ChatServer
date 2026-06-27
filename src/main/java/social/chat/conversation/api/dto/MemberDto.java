package social.chat.conversation.api.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MemberDto (
        @NotNull
        Long conversationId,
        @NotEmpty
        List<Long> userIds
){}
