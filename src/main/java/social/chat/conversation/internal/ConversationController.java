package social.chat.conversation.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.shared.dto.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("conversations")
public class ConversationController {
    ConversationService conversationService;

    @PostMapping
    public ResponseEntity<Response<?>> createConversation(@RequestBody ConversationDto conversationDto,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(conversationService.createConversation(Long.parseLong(jwt.getSubject()),
                        conversationDto));
    }

    @GetMapping
    public ResponseEntity<Response<?>> getConversation(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestParam(required = false) Long lastId,
                                                       Pageable pageable) {
        return ResponseEntity.ok(conversationService.getConversation(Long.parseLong(jwt.getSubject()),
                lastId, pageable));
    }
}
