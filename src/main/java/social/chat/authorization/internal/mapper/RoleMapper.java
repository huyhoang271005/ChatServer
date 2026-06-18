package social.chat.authorization.internal.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import social.chat.authorization.api.dto.PermissionDto;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.authorization.internal.entity.Permission;
import social.chat.authorization.internal.entity.Role;
import social.chat.authorization.internal.entity.RolePermission;

@Mapper(componentModel = "spring")
public interface RoleMapper {
    @Mapping(target = "permissions", source = "rolePermissions")
    RolePermissionDto toRolePermissionDto(Role role);

    @Mappings({
            @Mapping(target = "permissionId", source = "permission.permissionId"),
            @Mapping(target = "permissionName", source = "permission.permissionName"),
            @Mapping(target = "rolePermissionId", source = "rolePermissionId")
    })
    PermissionDto toPermissionDto(RolePermission rolePermission);

    Role toRole(RolePermissionDto rolePermissionDto);

    PermissionDto toPermissionDto(Permission permission);
}
