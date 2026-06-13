package social.chat.shared.websocket;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.message.api.dto.MessageDto;

@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataDto {
    WebsocketEventType type;
    ConversationDto conversation;
    MessageDto message;
}
