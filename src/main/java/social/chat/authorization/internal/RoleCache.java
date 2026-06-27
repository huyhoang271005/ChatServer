package social.chat.authorization.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.authorization.internal.mapper.RoleMapper;
import social.chat.authorization.internal.repository.RoleRepository;
import social.chat.shared.common.GlobalParamName;
import social.chat.shared.exception.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@CacheConfig(cacheNames = GlobalParamName.CacheName.ROLE)
public class RoleCache {
    RoleMapper roleMapper;
    RoleRepository roleRepository;

    @Transactional(readOnly = true)
    @Cacheable(key = "#roleId")
    public RolePermissionDto getRolePermissionsCache(Long roleId) {
        log.info("Cached role permissions for role {} ", roleId);
        return roleRepository.findRoleWithPermissions(roleId)
                .map(roleMapper::toRolePermissionDto)
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS));
    }

    @CacheEvict(key = "#roleId")
    public void deleteRolePermissionCache(Long roleId){
        log.info("Deleted role permissions for role {} ", roleId);
    }
}
