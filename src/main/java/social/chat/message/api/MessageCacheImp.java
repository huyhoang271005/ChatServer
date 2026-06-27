package social.chat.message.api;

import social.chat.message.api.dto.MessageDto;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MessageCacheImp {
    Optional<List<MessageDto>> getMessagesCache(Collection<Long> messageIds);
}
