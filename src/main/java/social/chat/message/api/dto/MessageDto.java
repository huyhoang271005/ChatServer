package social.chat.message.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto {
    Long messageId;
    Long conversationId;
    @NotBlank
    String text;
    String fileId;
    MessageType type;
    Boolean revoked;
    Long senderId;
    Instant createdAt;
}
