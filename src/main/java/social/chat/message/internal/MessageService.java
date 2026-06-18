package social.chat.message.internal;

import com.github.yitter.idgen.YitIdHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.conversation.api.ConversationImp;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.api.events.RegisterSaveMessageEvent;
import social.chat.profile.api.ProfileImp;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.dto.ResponseList;
import social.chat.shared.websocket.DataDto;
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
    MessageMapper messageMapper;
    ConversationImp conversationImp;
    ApplicationEventPublisher applicationEventPublisher;
    WebsocketService websocketService;
    private final ProfileImp profileImp;

    @Transactional(readOnly = true)
    public Response<ResponseList<MessageDto>> getMessages(Long conversationId, Long lastId, Pageable pageable) {
        Slice<Message> messages = messageRepository.findByConversationId(conversationId, lastId, pageable);
        List<MessageDto> messageDtos = messages.stream()
                .map(message -> {
                    MessageDto messageDto = messageMapper.toMessageDto(message);
                    if(messageDto.getRevoked() != null && messageDto.getRevoked()){
                        messageDto.setText(null);
                    }
                    return messageDto;
                })
                .toList();
        return Response.success(
                GlobalMessage.Success.GET,
                new ResponseList<>(
                        messageDtos,
                        messages.hasNext()
                )
        );
    }

    @Transactional
    public void sendMessage(Long userId, MessageDto messageDto) {
        ConversationDto conversationDto = conversationImp.getConversations(List.of(
                messageDto.getConversationId()
        )).getFirst();
        Long lastMessageId = YitIdHelper.nextId();
        messageDto.setMessageId(lastMessageId);
        Instant now = Instant.now();
        messageDto.setCreatedAt(now);
        messageDto.setSenderId(userId);
        conversationDto.setLastMessageText(messageDto.getText());
        conversationDto.setLastMessageTime(now);
        conversationDto.setLastMessageId(lastMessageId);
        conversationDto.setLastSenderId(userId);
        conversationDto.setLastMessageType(messageDto.getType());
        conversationDto.setLastMessageRevoked(false);
        String title = conversationDto.getUserConversations().size() == 2 ?
                profileImp.getShortProfiles(List.of(userId)).getFirst()
                .fullName() : conversationDto.getTitle();
        conversationDto.setTitle(title);
        conversationDto.getUserConversations()
                .stream()
                .filter(userConversationDto -> !userConversationDto
                        .getUserId().equals(userId))
                .forEach(userConversationDto -> userConversationDto
                        .setUnreadMessage(userConversationDto.getUnreadMessage() + 1));
        conversationImp.putConversation(conversationDto, false);
        websocketService.sendMessage(userId, DataDto.builder()
                .type(WebsocketEventType.NEW_MESSAGE)
                .conversation(conversationDto)
                .message(messageDto)
                .build());
        applicationEventPublisher.publishEvent(new RegisterSaveMessageEvent(title, messageDto));
    }

    public void typingMessage(Long userId, MessageDto messageDto) {
        ConversationDto conversationDto = conversationImp.getConversations(List
                .of(messageDto.getConversationId())).getFirst();
        DataDto dataDto = DataDto.builder()
                .type(WebsocketEventType.TYPING)
                .conversation(conversationDto)
                .message(messageDto)
                .build();
        websocketService.sendMessage(userId, dataDto);
    }

    public void unTypingMessage(Long userId, MessageDto messageDto) {
        ConversationDto conversationDto = conversationImp.getConversations(List
                .of(messageDto.getConversationId())).getFirst();
        messageDto.setSenderId(userId);
        DataDto dataDto = DataDto.builder()
                .type(WebsocketEventType.UNTYPING)
                .conversation(conversationDto)
                .message(messageDto)
                .build();
        websocketService.sendMessage(userId, dataDto);
    }

    @Transactional
    public void seenMessage(Long userId, MessageDto messageDto) {
        ConversationDto conversationDto = conversationImp.getConversations(List
                .of(messageDto.getConversationId())).getFirst();
        conversationDto.getUserConversations()
                .forEach(userConversationDto -> {
                    if(userConversationDto.getUserId().equals(userId)) {
                        userConversationDto.setLastMessageId(conversationDto.getLastMessageId());
                        userConversationDto.setUnreadMessage(0);
                    }
                });
        conversationImp.putConversation(conversationDto, false);
        DataDto dataDto = DataDto.builder()
                .type(WebsocketEventType.SEEN_MESSAGE)
                .conversation(conversationDto)
                .message(messageDto)
                .build();
        websocketService.sendMessage(
                userId,
                dataDto
        );
    }

    @Transactional
    public void revokeMessage(Long userId, MessageDto messageDto) {
        Message message = messageRepository.findById(messageDto.getMessageId())
                .orElse(null);
        if(message == null) {
            log.error("Message {} not found", messageDto.getMessageId());
            return;
        }
        if(!message.getSenderId().equals(userId)) {
            log.error("User {} cant revoke user message {}", userId, message.getSenderId());
            return;
        }
        messageDto.setRevoked(true);
        message.setRevoked(true);
        ConversationDto conversationDto = conversationImp.getConversations(List
                .of(messageDto.getConversationId())).getFirst();
        conversationDto.setLastMessageText(null);
        conversationDto.setLastMessageRevoked(true);
        conversationImp.putConversation(conversationDto, true);
        DataDto dataDto = DataDto.builder()
                .type(WebsocketEventType.REVOKE_MESSAGE)
                .conversation(conversationDto)
                .message(messageDto)
                .build();
        websocketService.sendMessage(userId, dataDto);
    }
}
