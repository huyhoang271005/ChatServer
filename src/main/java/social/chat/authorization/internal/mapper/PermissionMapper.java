package social.chat.authorization.internal.mapper;

import org.mapstruct.Mapper;
import social.chat.authorization.api.dto.PermissionDto;
import social.chat.authorization.internal.entity.Permission;

@Mapper(componentModel = "spring")
public interface PermissionMapper {
    PermissionDto toPermissionDto(Permission permission);
}
