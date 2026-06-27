package social.chat.message.internal.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.message.api.MessageCacheImp;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.internal.MessageMapper;
import social.chat.message.internal.cronjob.MessageCronjobProperties;
import social.chat.message.internal.repository.MessageRepository;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.cache.SafeCacheExecutor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CacheConfig(cacheNames = GlobalParamName.CacheName.MESSAGE)
public class MessageCache implements MessageCacheImp {
    Set<Long> messageIdsPending = ConcurrentHashMap.newKeySet();
    Lock messageLock = new ReentrantLock();
    MessageCronjobProperties messageCronjobProperties;
    MessageRepository messageRepository;
    SafeCacheExecutor safeCacheExecutor;
    MessageMapper messageMapper;

    @Override
    public Optional<List<MessageDto>> getMessagesCache(Collection<Long> messageIds) {
        return safeCacheExecutor.getCacheByIds(messageIds, GlobalParamName.CacheName.MESSAGE,
                MessageDto.class, messageLock, finalMissId ->
                        messageRepository.findAllById(finalMissId)
                        .stream()
                        .map(messageMapper::toMessageDto)
                        .toList(), MessageDto::getMessageId);
    }

    @CachePut(key = "#messageId")
    public MessageDto putMessageCache(Long messageId, MessageDto messageDto){
        if(messageId == null) {
            log.error("message need cache is null");
            return null;
        }
        messageIdsPending.add(messageId);
        log.info("Cached message {}", messageId);
        return messageDto;
    }

    public List<Long> getPendingMessageIds(){
        return safeCacheExecutor.getBatchPendingIds(messageIdsPending,
                messageCronjobProperties.getBatchSize());
    }

    public Collection<Long> getAllPendingMessageIds(){
        return safeCacheExecutor.getAllPendingIds(messageIdsPending);
    }

    public List<MessageDto> getMessagesByConversationId(List<Long> messageIds, Long conversationId) {
        Collection<Long> finalMessageIds = safeCacheExecutor.getIdsByFKId(messageIds,
                messageIdsPending, GlobalParamName.CacheName.MESSAGE,  MessageDto.class,
                messageDto -> messageDto.getConversationId().equals(conversationId));
        List<MessageDto> messageDtos = getMessagesCache(finalMessageIds)
                .orElse(Collections.emptyList())
                .stream()
                .map(messageMapper::toMessageDto)
                .toList();
        List<Long> messageIdsReply = messageDtos
                .stream()
                .map(MessageDto::getReplyMessageId)
                .filter(Objects::nonNull)
                .toList();
        Map<Long, MessageDto> messageDtosReplyMap = getMessagesCache(messageIdsReply)
                .orElse(Collections.emptyList())
                .stream()
                .map(messageDto -> {
                    if(Boolean.TRUE.equals(messageDto.getRevoked())){
                        MessageDto clone = messageMapper.toMessageDto(messageDto);
                        clone.setText(null);
                        clone.setType(null);
                        return clone;
                    }
                    return messageDto;
                })
                .collect(Collectors.toMap(MessageDto::getMessageId, Function.identity()));
        return messageDtos
                .stream()
                .peek(messageDto -> {
                    if(messageDto.getReplyMessageId() != null) {
                        MessageDto messageDtoReply = messageDtosReplyMap.get(messageDto.getReplyMessageId());
                        messageDto.setReplyMessageId(messageDtoReply.getMessageId());
                        if(!Boolean.TRUE.equals(messageDtoReply.getRevoked())){
                            messageDto.setReplyType(messageDtoReply.getType());
                            messageDto.setReplyText(messageDtoReply.getText());
                        }
                        messageDto.setReplyRevoked(messageDtoReply.getRevoked());
                    }
                })
                .sorted(Comparator.comparingLong(MessageDto::getMessageId).reversed())
                .toList();
    }

    @Transactional
    public void saveData(Collection<Long> messageIds){
        if(messageIds.isEmpty()){
            log.info("No messages need save");
            return;
        }
        safeCacheExecutor.saveDataWithIds(messageIds, messageIdsPending, longs ->
                getMessagesCache(longs)
                        .orElse(List.of()), messageDtos -> {
            Integer dataSave = messageRepository.saveAll(messageDtos
                    .stream()
                    .map(messageMapper::toMessage)
                    .toList()).size();
            log.info("Saved {} messages", dataSave);
        });
    }
}
