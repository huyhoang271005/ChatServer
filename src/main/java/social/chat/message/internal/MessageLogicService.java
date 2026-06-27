package social.chat.message.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.conversation.api.ConversationImp;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.message.api.MessageImp;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.internal.cache.MessageCache;
import social.chat.message.internal.cache.ReactorCache;
import social.chat.message.internal.repository.MessageRepository;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MessageLogicService implements MessageImp {
    MessageCache messageCache;
    ReactorCache reactorCache;
    MessageService messageService;
    MessageRepository messageRepository;
    ConversationImp conversationImp;

    @Override
    public void sendMessage(Long userId, String clientMsgId, MessageDto messageDto) {
        ConversationDto conversationDto = conversationImp.getConversations(List.of(messageDto.getConversationId()))
                        .getFirst();
        messageService.sendMessage(userId, messageDto, clientMsgId, conversationDto);
    }

    @Override
    public void saveBatchPendingMessage() {
        Collection<Long> messageIds = messageCache.getPendingMessageIds();
        messageCache.saveData(messageIds);
    }

    @Override
    public void saveAllPendingMessages() {
        Collection<Long> messageIds = messageCache.getAllPendingMessageIds();
        messageCache.saveData(messageIds);
    }

    @Override
    public void saveBatchPendingReactor() {
        Collection<Long> reactorIds = reactorCache.getPendingReactorIds();
        reactorCache.saveData(reactorIds);
    }

    @Override
    public void saveAllPendingReactor() {
        Collection<Long> reactorIds = reactorCache.getAllPendingReactorIds();
        reactorCache.saveData(reactorIds);
    }

    @Transactional
    @Override
    public void deleteByConversationId(Long conversationId) {
        Integer dataCount = messageRepository.deleteByConversationId(conversationId);
        log.error("Deleted {} messages for conversation {}", dataCount, conversationId);
    }
}
