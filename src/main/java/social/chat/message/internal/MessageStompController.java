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

@Controller
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageStompController {
    MessageService messageService;

    @MessageMapping("chat.send")
    public void sendMessage(@Payload MessageDto messageDto,
                            Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            String userIdStr = jwtAuth.getToken().getSubject();
            messageService.sendMessage(Long.parseLong(userIdStr), messageDto);
        } else {
            messageService.sendMessage(Long.parseLong(authentication.getName()), messageDto);
        }
    }
}
