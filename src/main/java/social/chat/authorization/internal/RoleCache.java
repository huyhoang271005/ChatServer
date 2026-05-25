package social.chat.authorization.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.authorization.internal.mapper.PermissionMapper;
import social.chat.authorization.internal.mapper.RoleMapper;
import social.chat.authorization.internal.repository.RoleRepository;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleCache {
    RoleMapper roleMapper;
    PermissionMapper permissionMapper;
    RoleRepository roleRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "roles", key = "#roleId")
    public RolePermissionDto getRolePermissionsCache(Long roleId) {
        log.info("Cache role permissions for role {} ", roleId);
        return roleRepository.findById(roleId)
                .map(role -> {
                    RolePermissionDto rolePermissionDto = roleMapper.toRolePermissionDto(role);
                    rolePermissionDto.setPermissions(role.getRolePermissions().stream()
                            .map(rolePermission -> permissionMapper.toPermissionDto(rolePermission.getPermission()))
                            .toList());
                    return rolePermissionDto;
                })
                .orElse(null);
    }

    @CacheEvict(value = "roles", key = "#roleId")
    public void deleteRolePermissionCache(Long roleId){
        log.info("Deleted role permissions for role {} ", roleId);
    }
}
