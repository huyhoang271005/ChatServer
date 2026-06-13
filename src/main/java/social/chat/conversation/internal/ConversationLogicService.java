package social.chat.conversation.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.conversation.api.ConversationImp;
import social.chat.conversation.api.dto.ConversationDto;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ConversationLogicService implements ConversationImp {
    ConversationCache conversationCache;

    @Override
    @Transactional
    public void putConversation(ConversationDto conversationDto, boolean saveDb) {
        conversationCache.updateConversation(conversationDto.getConversationId(), conversationDto,
                saveDb);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getConversations(List<Long> conversationIds) {
        return conversationCache.getConversations(conversationIds);
    }
}
