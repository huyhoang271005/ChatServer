package social.chat.message.internal;

import jakarta.persistence.EntityManager;
import org.checkerframework.checker.units.qual.A;
import org.mapstruct.*;
import social.chat.message.api.dto.MessageDto;
import social.chat.message.api.dto.ReactorCacheDto;
import social.chat.message.internal.entity.Message;
import social.chat.message.internal.entity.Reactor;
import social.chat.message.internal.repository.MessageRepository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface MessageMapper {
    @Mapping(target = "isNew", source = "new")
    MessageDto toMessageDto(Message message);
    @Mapping(target = "isNew", source = "new")
    Message toMessage(MessageDto messageDto);
    @Mapping(target = "isNew", source = "new")
    MessageDto toMessageDto(MessageDto messageDto);
    @Mappings({
            @Mapping(target = "isNew", source = "new"),
            @Mapping(target = "messageId", source = "message.messageId")
    })
    ReactorCacheDto toReactorDto(Reactor reactor);
    @Mapping(target = "isNew", source = "new")
    Reactor toReactor(ReactorCacheDto reactorCacheDto, @Context MessageRepository messageRepository);

    @AfterMapping
    default void handleReactorCountForMessageDto(@MappingTarget MessageDto messageDto) {
        if(messageDto.getReactorCount() == null) {
            messageDto.setReactorCount(new HashMap<>());
        }
    }
    @AfterMapping
    default void handleReactorCountForMessage(@MappingTarget Message message){
        if(message.getReactorCount() == null || message.getReactorCount().isEmpty()) {
            message.setReactorCount(null);
        }
    }

    @AfterMapping
    default void handleMapMessageForReactor(ReactorCacheDto reactorCacheDto,
                                            @MappingTarget Reactor reactor,
                                            @Context MessageRepository messageRepository) {
        if(reactorCacheDto.getMessageId() != null) {
            Message message = messageRepository.getReferenceById(reactorCacheDto.getMessageId());
            reactor.setMessage(message);
        }
    }

    default String mapEnumToEmoji(ReactionType reactionType) {
        if(reactionType == null) {
            return null;
        }
        return reactionType.getEmoji();
    }

    default ReactionType mapEmojiToEnum(String emoji) {
        if (emoji == null) return null;
        for (ReactionType type : ReactionType.values()) {
            if (type.getEmoji().equals(emoji)) {
                return type;
            }
        }
        return null;
    }

    default Map<ReactionType, Integer> stringIntegerMapToReactionTypeIntegerMap(Map<String, Integer> map) {
        if (map == null) {
            return null;
        }
        Map<ReactionType, Integer> targetMap = new LinkedHashMap<>(map.size());
        for (Map.Entry<String, Integer> entry : map.entrySet()) {
            ReactionType key = mapEmojiToEnum(entry.getKey());
            targetMap.put(key, entry.getValue());
        }
        return targetMap;
    }
}
