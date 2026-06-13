package social.chat.message.internal;

import com.github.yitter.idgen.YitIdHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.conversation.api.ConversationImp;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.api.dto.UserConversationDto;
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
        String title = conversationDto.getUserConversations().size() == 2 ?
                profileImp.getShortProfiles(List.of(userId)).getFirst()
                .getFullName() : conversationDto.getTitle();
        conversationDto.setTitle(title);
        websocketService.sendMessage(conversationDto.getUserConversations()
                .stream()
                .map(UserConversationDto::getUserId)
                .toList(), userId, DataDto.builder()
                .type(WebsocketEventType.NEW_MESSAGE)
                .conversation(conversationDto)
                .message(messageDto)
                .build());
        applicationEventPublisher.publishEvent(new RegisterSaveMessageEvent(title, messageDto));
    }
}
