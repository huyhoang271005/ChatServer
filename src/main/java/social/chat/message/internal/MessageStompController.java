package social.chat.message.internal;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import social.chat.message.api.dto.GroupValidMessage;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.api.dto.ReactorRequest;
import social.chat.shared.websocket.DataDto;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageStompController {
    MessageService messageService;
    ReactorService reactorService;

    private Long getUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String userIdStr = jwtAuth.getToken().getSubject();
            return Long.parseLong(userIdStr);
        } else {
            return Long.parseLong(authentication.getName());
        }
    }

    @MessageMapping("chat.send")
    public void sendMessage(@Payload @Validated({GroupValidMessage.onlyConversationId.class,
                                Default.class}) DataDto<MessageDto> dataDto,
                            Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.checkRoleThenSendMessage(userId, dataDto.data(), dataDto.clientMsgId());
    }

    @MessageMapping("chat.typing")
    public void typingMessage(@Payload @Validated(GroupValidMessage.onlyConversationId.class)
                                  DataDto<MessageDto> dataDto,
                              Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.typingMessage(userId, dataDto.data(), dataDto.clientMsgId());
    }

    @MessageMapping("chat.untyping")
    public void unTypingMessage(@Payload @Validated(GroupValidMessage.onlyConversationId.class)
                                    DataDto<MessageDto> dataDto,
                                Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.unTypingMessage(userId, dataDto.data(), dataDto.clientMsgId());
    }

    @MessageMapping("chat.seen")
    public void seenMessage(@Payload @Validated({GroupValidMessage.onlyMessageId.class,
                                GroupValidMessage.onlyConversationId.class}) DataDto<MessageDto> dataDto,
                            Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.seenMessage(userId, dataDto.data(), dataDto.clientMsgId());
    }

    @MessageMapping("chat.revoke")
    public void revokeMessage(@Payload @Validated({GroupValidMessage.onlyMessageId.class,
                                          GroupValidMessage.onlyConversationId.class})
                                  DataDto<MessageDto> dataDto,
                                Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.revokeMessage(userId, dataDto.data(), dataDto.clientMsgId());
    }

    @MessageMapping("chat.reaction")
    public void reactionMessage(@Payload @Valid DataDto<ReactorRequest> dataDto,
                                Authentication authentication) {
        Long userId = getUserId(authentication);
        reactorService.reactionMessage(userId, dataDto.data(), dataDto.clientMsgId());
    }

    @MessageMapping("chat.unreaction")
    public void unReactionMessage(@Payload @Valid DataDto<ReactorRequest> dataDto,
                                  Authentication authentication) {
        Long userId = getUserId(authentication);
        reactorService.unReactionMessage(userId, dataDto.data(), dataDto.clientMsgId());
    }
}
