package social.chat.shared.websocket;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.ConvertGroup;
import social.chat.conversation.api.dto.GroupValidConversation;
import social.chat.message.api.dto.GroupValidMessage;

public record DataDto<T> (
    WebsocketEventType type,
    Long senderId,
    @NotBlank
    String clientMsgId,
    @Valid
    @ConvertGroup(from = GroupValidConversation.onlyConversationId.class,
            to = GroupValidConversation.onlyConversationId.class)
    @ConvertGroup(from = GroupValidMessage.onlyConversationId.class,
            to = GroupValidMessage.onlyConversationId.class)
    @ConvertGroup(from = GroupValidMessage.onlyMessageId.class,
            to = GroupValidMessage.onlyMessageId.class)
    T data
){}
