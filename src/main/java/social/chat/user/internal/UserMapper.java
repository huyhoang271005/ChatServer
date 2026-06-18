package social.chat.user.internal;

import org.mapstruct.Mapper;
import social.chat.user.api.dto.ExtendUser;
import social.chat.user.api.dto.UserCacheDto;

@Mapper(componentModel = "spring")
public interface UserMapper {
    ExtendUser toExtendUser(User user);
    UserCacheDto toUserCacheDto(User user);
}
