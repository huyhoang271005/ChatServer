package social.chat.profile.internal.mapper;

import org.mapstruct.Mapper;
import social.chat.profile.api.dto.EmailDto;
import social.chat.profile.internal.entity.Email;

@Mapper(componentModel = "spring")
public interface EmailMapper {
    EmailDto toEmailDto(Email email);
}
