package social.chat.message.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import social.chat.message.api.dto.MessageDto;

import java.util.List;

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageStompController {
    MessageService messageService;

    private Long getUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String userIdStr = jwtAuth.getToken().getSubject();
            return Long.parseLong(userIdStr);
        } else {
            return Long.parseLong(authentication.getName());
        }
    }

    @MessageMapping("chat.send")
    public void sendMessage(@Payload MessageDto messageDto,
                            Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.sendMessage(userId, messageDto);
    }

    @MessageMapping("chat.typing")
    public void typingMessage(@Payload MessageDto messageDto,
                              Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.typingMessage(userId, messageDto);
    }

    @MessageMapping("chat.untyping")
    public void unTypingMessage(@Payload MessageDto messageDto,
                                Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.unTypingMessage(userId, messageDto);
    }

    @MessageMapping("chat.seen")
    public void seenMessage(@Payload MessageDto messageDto,
                            Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.seenMessage(userId, messageDto);
    }

    @MessageMapping("chat.revoke")
    public void revokeMessage(@Payload MessageDto messageDto,
                                Authentication authentication) {
        Long userId = getUserId(authentication);
        messageService.revokeMessage(userId, messageDto);
    }

    @MessageMapping("chat.add")
    public void addMember(@Payload List<Long> userIds, Authentication authentication) {
        Long userId = getUserId(authentication);
    }

    @MessageMapping("chat.delete")
    public void deleteMember(@Payload List<Long> userIds, Authentication authentication) {
        Long userId = getUserId(authentication);
    }

    @MessageMapping("chat.leave")
    public void leaveConversation(@Payload MessageDto messageDto, Authentication authentication) {
        Long userId = getUserId(authentication);
    }
}
