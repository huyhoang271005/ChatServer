package social.chat.conversation.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.conversation.internal.ConversationRole;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserConversationDto {
    Long userId;
    Long lastMessageId;
    ConversationRole conversationRole;
    Integer unreadMessage;
    Instant joinedAt;


}
