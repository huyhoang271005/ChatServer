package social.chat.conversation.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import social.chat.conversation.internal.ConversationRole;
import social.chat.message.api.dto.MessageType;
import social.chat.shared.common.GlobalMessage;

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
    @NotNull(groups = GroupValidConversation.onlyConversationId.class)
    Long conversationId;
    @NotBlank
    @Size(min = 1, max = 125, message = GlobalMessage.Error.TEXT_OVERFLOW)
    String title;
    String conversationAvatarUrl;
    String conversationAvatarId;
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
