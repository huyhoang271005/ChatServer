package social.chat.message.api;

import org.springframework.modulith.NamedInterface;
import social.chat.message.api.dto.MessageDto;

import java.util.List;

@NamedInterface
public interface MessageImp {
    void sendMessage(Long userId, String clientMsgId, MessageDto messageDto);
    void saveBatchPendingMessage();
    void saveAllPendingMessages();
    void saveBatchPendingReactor();
    void saveAllPendingReactor();
    void deleteByConversationId(Long conversationId);
}
