package social.chat.conversation.internal;

import org.mapstruct.*;
import social.chat.conversation.api.dto.ConversationDto;
import social.chat.conversation.api.dto.UserConversationDto;
import social.chat.conversation.internal.entity.Conversation;
import social.chat.conversation.internal.entity.UserConversation;

import java.util.ArrayList;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ConversationMapper {
    @Mapping(target = "isNew", source = "new")
    ConversationDto toConversationDto(Conversation conversation);
    @Mapping(target = "isNew", source = "new")
    Conversation toConversation(ConversationDto conversationDto);
    @Mappings({
            @Mapping(target = "conversationId", source = "conversation.conversationId"),
            @Mapping(target = "isNew", source = "new")
    })
    UserConversationDto toUserConversationDto(UserConversation userConversation);
    @Mappings({
            @Mapping(target = "conversation.conversationId", source = "conversationId"),
            @Mapping(target = "isNew", source = "new")
    })
    UserConversation toUserConversation(UserConversationDto userConversationDto);
    @Mappings({
            @Mapping(target = "conversationId", source = "conversationId", ignore = true),
            @Mapping(target = "group", source = "group", ignore = true),
            @Mapping(target = "new", source = "new")
    })

    void updateConversation(ConversationDto conversationDto,
                            @MappingTarget ConversationDto conversationDto1);

    @AfterMapping
    default void handleConversationDto(@MappingTarget ConversationDto conversationDto) {
        if(conversationDto.getRolesCanChat() == null){
            conversationDto.setRolesCanChat(new ArrayList<>());
        }
    }

    @AfterMapping
    default void handleConversation(@MappingTarget Conversation conversation){
        if(conversation.getRolesCanChat() == null || conversation.getRolesCanChat().isEmpty()) {
            conversation.setRolesCanChat(null);
        }
    }
}
