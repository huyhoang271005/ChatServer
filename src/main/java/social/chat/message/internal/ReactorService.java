package social.chat.message.internal;

import com.github.yitter.idgen.YitIdHelper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.api.dto.ReactorCacheDto;
import social.chat.message.api.dto.ReactorRequest;
import social.chat.message.internal.cache.MessageCache;
import social.chat.message.internal.cache.ReactorCache;
import social.chat.message.internal.repository.ReactorRepository;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.shared.websocket.WebsocketEventType;
import social.chat.shared.websocket.WebsocketService;

import java.util.HashMap;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ReactorService {
    ReactorCache reactorCache;
    MessageCache messageCache;
    WebsocketService websocketService;
    ReactorRepository reactorRepository;

    public void reactionMessage(Long userId, ReactorRequest reactorRequest, String clientMsgId) {
        ReactorCacheDto reactorCacheDto = reactorCache.getReactorCacheByMessageIdAndUserId(userId,
                        reactorRequest.messageId())
                .orElseGet(() -> ReactorCacheDto.builder()
                        .reactorId(YitIdHelper.nextId())
                        .userId(userId)
                        .messageId(reactorRequest.messageId())
                        .reactionCount(new HashMap<>())
                        .isNew(true)
                        .build());
        reactorCache.updateReactor(reactorCacheDto.getReactorId(), reactorCacheDto);
        MessageDto messageDto = messageCache.getMessagesCache(List.of(reactorRequest.messageId()))
                .orElseThrow(() -> new EntityNotFoundException(MessageMessages.NOT_EXISTS))
                .getFirst();
        String emoji = reactorRequest.reactionType().getEmoji();
        messageDto.getReactorCount().merge(emoji, 1, Integer::sum);
        reactorCacheDto.getReactionCount().merge(emoji, 1, Integer::sum);
        messageCache.putMessageCache(messageDto.getMessageId(), messageDto);
        websocketService.sendMessageToConversation(userId, clientMsgId, WebsocketEventType.UPDATE_MESSAGE,
                messageDto.getConversationId(), messageDto.getMessageId());
    }

    public void unReactionMessage(Long userId, ReactorRequest reactorRequest, String clientMsgId) {
        MessageDto messageDto = messageCache.getMessagesCache(List.of(reactorRequest.messageId()))
                .orElseThrow(() -> new EntityNotFoundException(MessageMessages.NOT_EXISTS))
                .getFirst();
        List<ReactorCacheDto> reactorCacheDtos = reactorCache.getReactorsByMessageId(List.of(),
                reactorRequest.messageId());
        if(!reactorCacheDtos.isEmpty()) {
            reactorCacheDtos
                    .stream()
                    .filter(reactorCacheDto1 -> reactorCacheDto1.getUserId()
                            .equals(userId))
                    .findAny()
                    .ifPresent(reactorCacheDto -> {
                        reactorRepository.deleteById(reactorCacheDto.getReactorId());
                        reactorCacheDto.getReactionCount().forEach((key, value) ->
                                messageDto.getReactorCount()
                                        .merge(key, - value, Integer::sum));
                        reactorCache.deleteReactor(reactorCacheDto.getReactorId());
                    });
        }
        websocketService.sendMessageToConversation(userId, clientMsgId, WebsocketEventType.UPDATE_MESSAGE,
                messageDto.getConversationId(), messageDto.getMessageId());
    }

    public Response<List<ReactorCacheDto>> getReactors(Long messageId) {
        List<Long> reactorIds = reactorRepository.getReactorIdsByUserIdAndMessageId(messageId);
        return Response.success(
                GlobalMessage.Success.GET,
                reactorCache.getReactorsByMessageId(reactorIds, messageId)
        );
    }
}
