package social.chat.user_presence.internal;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Mappings;
import social.chat.user_presence.api.UserPresenceDto;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserPresenceMapper {
    @Mapping(target = "isNew", source = "isNew")
    UserPresence toUserPresence(UserPresenceDto userPresenceDto);

    @Mappings({
            @Mapping(target = "isNew", source = "new"),
            @Mapping(target = "count", constant = "0")
    })
    UserPresenceDto toUserPresenceDto(UserPresence userPresence);
}
