package social.chat.conversation.api;

import org.springframework.modulith.NamedInterface;
import social.chat.conversation.api.dto.ConversationDto;

import java.util.List;

@NamedInterface
public interface ConversationImp {
    void putConversation(ConversationDto conversationDto, boolean saveDb);
    List<ConversationDto> getConversations(List<Long> conversationIds);
}
