package social.chat.conversation.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
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
    Long conversationId;
    String title;
    String conversationAvatar;
    Long lastSenderId;
    String lastMessageText;
    MessageType lastMessageType;
    Instant lastMessageTime;
    Long lastMessageId;
    boolean lastMessageRevoked;
    boolean group;
    Long createdBy;
    Instant createdAt;
    Integer unreadMessage;
    List<UserConversationDto> userConversations;
}
