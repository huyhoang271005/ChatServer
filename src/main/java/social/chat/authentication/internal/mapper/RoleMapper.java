package social.chat.authentication.internal.mapper;

import org.mapstruct.Mapper;
import social.chat.authentication.api.dto.RolePermissionDto;
import social.chat.authentication.internal.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    RolePermissionDto toRolePermissionDto(Role role);
    Role toRole(RolePermissionDto rolePermissionDto);
}
