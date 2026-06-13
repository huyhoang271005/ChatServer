package social.chat.conversation.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.internal.entity.Conversation;
import social.chat.conversation.internal.repository.ConversationRepository;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationCache {
    CacheManager cacheManager;
    ConversationRepository conversationRepository;
    ConversationMapper conversationMapper;

    @Transactional(readOnly = true)
    public List<ConversationDto> getConversations(List<Long> conversationIds) {
        List<ConversationDto> result = new ArrayList<>();
        List<Long> missingConversationIds = new ArrayList<>();

        Cache conversationCache = cacheManager.getCache("conversations");

        for(Long correlationId : conversationIds) {
            if(conversationCache != null){
                ConversationDto conversationDto = conversationCache.get(correlationId, ConversationDto.class);
                if(conversationDto == null){
                    missingConversationIds.add(correlationId);
                }
                else {
                    result.add(conversationDto);
                }
            }
        }

        if(!missingConversationIds.isEmpty()) {
            conversationRepository.findByConversationIds(missingConversationIds)
                    .stream()
                    .map(conversationMapper::toConversationDto)
                    .forEach(conversationDto -> {
                        conversationCache.put(conversationDto.getConversationId(), conversationDto);
                        result.add(conversationDto);
                    });
        }
        return result;
    }

    @CachePut(cacheNames = "conversations", key = "#conversationId")
    @Transactional
    public ConversationDto updateConversation(Long conversationId, ConversationDto conversationDto,
                                              Boolean saveDb) {
        if(saveDb){
            Conversation conversation = conversationRepository.findById(conversationId)
                            .orElseGet(Conversation::new);
            conversationMapper.updateConversation(conversationDto, conversation);
            conversationRepository.save(conversation);
        }
        return conversationDto;
    }

}
