package social.chat.message.api.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.shared.common.GlobalMessage;

import java.time.Instant;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MessageDto {
    @NotNull(groups = {GroupValidMessage.onlyMessageId.class})
    Long messageId;
    @NotNull(groups = {GroupValidMessage.onlyConversationId.class})
    Long conversationId;
    @NotBlank
    @Size(min = 2, max = 1000, message = GlobalMessage.Error.TEXT_OVERFLOW)
    String text;
    String fileId;
    @NotNull
    MessageType type;
    Boolean revoked;
    Long senderId;
    Long replyMessageId;
    String replyText;
    MessageType replyType;
    Boolean replyRevoked;
    Map<String, Integer> reactorCount;
    Instant createdAt;
    @JsonIgnore
    boolean isNew;
}
