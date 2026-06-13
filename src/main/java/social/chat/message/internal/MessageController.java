package social.chat.message.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import social.chat.shared.dto.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("messages")
public class MessageController {
    MessageService messageService;

    @GetMapping("{conversationId}")
    public ResponseEntity<Response<?>> getMessages(@PathVariable Long conversationId,
                                                   @RequestParam(required = false) Long lastId,
                                                   Pageable pageable) {
        return ResponseEntity.ok(messageService.getMessages(conversationId, lastId, pageable));
    }
}
