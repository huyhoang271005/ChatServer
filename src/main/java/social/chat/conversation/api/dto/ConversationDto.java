package social.chat.conversation.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import social.chat.conversation.internal.ConversationRole;
import social.chat.message.api.dto.MessageType;

import java.time.Instant;
import java.util.List;

@NamedInterface
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ConversationDto {
    @NotNull(groups = GroupValidConversation.onlyConversationId.class, message = "conversationId not be null")
    Long conversationId;
    String title;
    String conversationAvatarUrl;
    Long lastSenderId;
    String lastMessageText;
    MessageType lastMessageType;
    Instant lastMessageTime;
    Long lastMessageId;
    boolean lastMessageRevoked;
    boolean group;
    Long createdBy;
    List<ConversationRole> rolesCanChat;
    Instant createdAt;
    Instant updatedAt;
    List<UserConversationDto> userConversations;
    @JsonIgnore
    boolean isNew;
}
