package social.chat.message.internal;

import com.github.yitter.idgen.YitIdHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import social.chat.conversation.api.ConversationImp;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.internal.cache.MessageCache;
import social.chat.message.internal.repository.MessageRepository;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.dto.ResponseList;
import social.chat.shared.exception.ConflictException;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.websocket.WebsocketEventType;
import social.chat.shared.websocket.WebsocketService;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageService {
    MessageRepository messageRepository;
    ConversationImp conversationImp;
    MessageCache messageCache;
    WebsocketService websocketService;

    public Response<ResponseList<MessageDto>> getMessages(Long conversationId, Long lastId, Pageable pageable) {
        Slice<Long> messageIdsSlice = messageRepository.findByConversationId(conversationId, lastId, pageable);
        List<MessageDto> messageDtos = messageCache.getMessagesByConversationId(messageIdsSlice.getContent(),
                conversationId);
        return Response.success(
                GlobalMessage.Success.GET,
                new ResponseList<>(
                        messageDtos,
                        messageIdsSlice.hasNext()
                )
        );
    }

    public void checkRoleThenSendMessage(Long userId, MessageDto messageDto, String clientMsgId){
        ConversationDto conversationDto = conversationImp.getConversations(List.of(
                messageDto.getConversationId()
        )).getFirst();
        conversationDto.getUserConversations()
                .stream()
                .filter(userConversationDto -> userConversationDto
                        .getUserId().equals(userId))
                .findAny()
                .ifPresent(userConversationDto -> {
                    if(!conversationDto.getRolesCanChat()
                            .contains(userConversationDto.getConversationRole())){
                        throw new ConflictException(MessageMessages.LIMIT);
                    }
                    sendMessage(userId, messageDto, clientMsgId, conversationDto);

                });
    }

    public void sendMessage(Long userId, MessageDto messageDto, String clientMsgId, ConversationDto conversationDto) {
        if(messageDto.getText() == null || messageDto.getText().isEmpty()){
            return;
        }
        Long lastMessageId = YitIdHelper.nextId();
        messageDto.setMessageId(lastMessageId);
        messageDto.setNew(true);
        Instant now = Instant.now();
        messageDto.setCreatedAt(now);
        messageDto.setSenderId(userId);
        conversationDto.setLastMessageText(messageDto.getText());
        conversationDto.setLastMessageTime(now);
        conversationDto.setLastMessageId(lastMessageId);
        conversationDto.setLastSenderId(userId);
        conversationDto.setLastMessageType(messageDto.getType());
        conversationDto.setLastMessageRevoked(false);
        conversationDto.setUpdatedAt(Instant.now());
        if(messageDto.getReplyMessageId() != null){
            MessageDto messageDtoReply = messageCache.getMessagesCache(List.of(messageDto.getReplyMessageId()))
                    .orElseThrow(() -> new EntityNotFoundException(MessageMessages.NOT_EXISTS))
                    .getFirst();
            if(messageDtoReply != null){
                log.info("Found reply message {}", messageDtoReply.getMessageId());
                messageDto.setReplyMessageId(messageDtoReply.getMessageId());
                messageDto.setReplyText(messageDtoReply.getText());
                messageDto.setReplyType(messageDtoReply.getType());
                messageDto.setRevoked(messageDtoReply.getRevoked());
            }
        }
        String title = conversationDto.getUserConversations().size() == 2 ?
                conversationDto.getUserConversations()
                .stream()
                .filter(userConversationDto -> userConversationDto.getUserId().equals(userId))
                .toList().getFirst().getFullName(): conversationDto.getTitle();
        conversationDto.setTitle(title);
        conversationDto.getUserConversations()
                .stream()
                .filter(userConversationDto -> !userConversationDto
                        .getUserId().equals(userId))
                .forEach(userConversationDto -> userConversationDto
                        .setUnreadMessage(userConversationDto.getUnreadMessage() + 1));
        conversationImp.putConversation(conversationDto);
        messageCache.putMessageCache(messageDto.getMessageId(), messageDto);
        websocketService.sendMessageToConversation(userId, clientMsgId, WebsocketEventType.NEW_MESSAGE,
                conversationDto.getConversationId(), messageDto.getMessageId());
    }

    public void typingMessage(Long userId, MessageDto messageDto, String clientMsgId) {
        ConversationDto conversationDto = conversationImp.getConversations(List
                .of(messageDto.getConversationId())).getFirst();
        websocketService.sendMessageToConversation(userId, clientMsgId, WebsocketEventType.TYPING,
                conversationDto.getConversationId(), messageDto.getMessageId());
    }

    public void unTypingMessage(Long userId, MessageDto messageDto, String clientMsgId) {
        ConversationDto conversationDto = conversationImp.getConversations(List
                .of(messageDto.getConversationId())).getFirst();
        messageDto.setSenderId(userId);
        websocketService.sendMessageToConversation(userId, clientMsgId, WebsocketEventType.UNTYPING,
                conversationDto.getConversationId(), messageDto.getMessageId());
    }

    public void seenMessage(Long userId, MessageDto messageDto, String clientMsgId) {
        ConversationDto conversationDto = conversationImp.getConversations(List
                .of(messageDto.getConversationId())).getFirst();
        conversationDto.getUserConversations()
                .forEach(userConversationDto -> {
                    if(userConversationDto.getUserId().equals(userId)) {
                        userConversationDto.setLastMessageId(conversationDto.getLastMessageId());
                        userConversationDto.setUnreadMessage(0);
                    }
                });
        conversationImp.putConversation(conversationDto);
        websocketService.sendMessageToConversation(userId, clientMsgId, WebsocketEventType.SEEN_MESSAGE,
                conversationDto.getConversationId(), messageDto.getMessageId());
    }

    public void revokeMessage(Long userId, MessageDto messageDtoCurrent, String clientMsgId) {
        MessageDto messageDto = messageCache.getMessagesCache(List.of(messageDtoCurrent.getMessageId()))
                .orElseThrow(() -> new EntityNotFoundException(MessageMessages.NOT_EXISTS))
                .getFirst();
        if(messageDto == null) {
            log.error("Message {} not found", messageDtoCurrent.getMessageId());
            return;
        }
        if(!messageDto.getSenderId().equals(userId)) {
            log.error("User {} cant revoke user message {}", userId, messageDto.getSenderId());
            return;
        }
        messageDto.setRevoked(true);
        ConversationDto conversationDto = conversationImp.getConversations(List
                .of(messageDto.getConversationId())).getFirst();
        conversationDto.setLastMessageText(null);
        conversationDto.setLastMessageRevoked(true);
        conversationImp.putConversation(conversationDto);
        messageCache.putMessageCache(messageDto.getMessageId(), messageDto);
        websocketService.sendMessageToConversation(userId, clientMsgId, WebsocketEventType.REVOKE_MESSAGE,
                conversationDto.getConversationId(), messageDto.getMessageId());
    }
}
