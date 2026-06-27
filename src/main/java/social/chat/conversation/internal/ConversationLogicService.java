package social.chat.conversation.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import social.chat.conversation.api.ConversationImp;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.shared.exception.EntityNotFoundException;

import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationLogicService implements ConversationImp {
    ConversationCache conversationCache;

    @Override
    public void putConversation(ConversationDto conversationDto) {
        conversationCache.updateConversation(conversationDto.getConversationId(), conversationDto);
    }

    @Override
    public List<ConversationDto> getConversations(List<Long> conversationIds) {
        return conversationCache.getConversationsCache(conversationIds)
                .orElseThrow(() -> new EntityNotFoundException(ConversationMessage.NOT_EXISTS));
    }

    @Override
    public void saveBatchPendingConversations() {
        Collection<Long> conversationIds = conversationCache.getPendingConversationIds();
        conversationCache.saveData(conversationIds);
    }

    @Override
    public void saveAllPendingConversations() {
        Collection<Long> conversationIds = conversationCache.getAllPendingConversationIds();
        conversationCache.saveData(conversationIds);
    }
}
