package social.chat.authentication.internal.mapper;

import org.mapstruct.Mapper;
import social.chat.authentication.api.dto.PermissionDto;
import social.chat.authentication.internal.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDto toPermissionDto(Permission permission);
}
