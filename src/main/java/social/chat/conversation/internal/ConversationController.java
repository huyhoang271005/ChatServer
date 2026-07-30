package social.chat.conversation.internal;

import jakarta.validation.Valid;
import jakarta.validation.groups.Default;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.api.dto.MemberDto;
import social.chat.conversation.api.dto.UserConversationDto;
import social.chat.message.api.dto.GroupValidMessage;
import social.chat.shared.dto.Response;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequestMapping("conversations")
public class ConversationController {
    ConversationService conversationService;

    @PostMapping
    public ResponseEntity<Response<?>> createConversation(@RequestBody @Valid ConversationDto conversationDto,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(conversationService.createConversation(Long.parseLong(jwt.getSubject()),
                        conversationDto));
    }

    @PutMapping
    public ResponseEntity<Response<?>> updateConversation(@RequestBody @Validated({GroupValidMessage.onlyConversationId.class,
                                                              Default.class}) ConversationDto conversationDto,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(conversationService.updateConversation(Long.parseLong(jwt.getSubject()),
                conversationDto));
    }

    @GetMapping
    public ResponseEntity<Response<?>> getConversation(@AuthenticationPrincipal Jwt jwt,
                                                       @RequestParam(required = false) Long lastId,
                                                       @RequestParam(required = false) String title,
                                                       Pageable pageable) {
        return ResponseEntity.ok(conversationService.getConversations(Long.parseLong(jwt.getSubject()),
                lastId, title, pageable));
    }

    @PostMapping("member")
    public ResponseEntity<Response<?>> addMember(@AuthenticationPrincipal Jwt jwt,
                                                 @Valid @RequestBody MemberDto memberDto) {
        return ResponseEntity.ok(conversationService.addMember(Long.parseLong(jwt.getSubject()),
                memberDto));
    }

    @DeleteMapping("{conversationId}/member/{userId}")
    public ResponseEntity<Response<?>> removeMember(@AuthenticationPrincipal Jwt jwt,
                                                    @PathVariable Long conversationId,
                                                    @PathVariable Long userId) {
        return ResponseEntity.ok(conversationService.removeMember(Long.parseLong(jwt.getSubject()),
                conversationId, userId));
    }

    @PutMapping("{conversationId}/member/{userId}")
    public ResponseEntity<Response<?>> updateRoleUser(@AuthenticationPrincipal Jwt jwt,
                                                      @RequestBody UserConversationDto userConversationDto,
                                                      @PathVariable Long userId,
                                                      @PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.changeConversationRole(Long.parseLong(jwt.getSubject()),
                userId, conversationId, userConversationDto));
    }

    @GetMapping("{conversationId}")
    public ResponseEntity<Response<?>> getConversation(@PathVariable Long conversationId,
                                                       @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(conversationService.getConversation(Long.parseLong(jwt.getSubject()),
                conversationId));
    }

    @DeleteMapping("{conversationId}")
    public ResponseEntity<Response<?>> deleteConversation(@PathVariable Long conversationId,
                                                          @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(conversationService.closeConversation(Long.parseLong(jwt.getSubject()),
                conversationId));
    }

    @PatchMapping("{conversationId}/leave")
    public ResponseEntity<Response<?>> leaveConversation(@AuthenticationPrincipal Jwt jwt,
                                                         @PathVariable Long conversationId) {
        return ResponseEntity.ok(conversationService.leaveConversation(Long.parseLong(jwt.getSubject()),
                conversationId));
    }


    //patch conversations/{conversationId} Soft delete conversation
}
