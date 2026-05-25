package social.chat.authorization.internal.mapper;

import org.mapstruct.Mapper;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.authorization.internal.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RolePermissionDto toRolePermissionDto(Role role);
    Role toRole(RolePermissionDto rolePermissionDto);
}
