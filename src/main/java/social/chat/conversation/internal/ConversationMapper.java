package social.chat.conversation.internal;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.internal.entity.Conversation;

@Mapper(componentModel = "spring")
public interface ConversationMapper {
    ConversationDto toConversationDto(Conversation conversation);
    Conversation toConversation(ConversationDto conversationDto);
    void updateConversation(ConversationDto conversationDto, @MappingTarget Conversation conversation);
}
