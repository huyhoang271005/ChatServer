package social.chat.authorization.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.authorization.internal.entity.Role;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.repository.RoleRepository;
import social.chat.shared.exception.EntityNotFoundException;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationLogicService implements AuthorizationImp {
    RoleRepository roleRepository;
    RoleCache roleCache;

    @Override
    @Transactional(readOnly = true)
    public Long getRoleIdByRoleUser() {
        return roleRepository.findByRoleName(RoleDefault.USER.name())
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS))
                .getRoleId();
    }

    @Override
    @Transactional(readOnly = true)
    public RolePermissionDto getRolePermissionByRoleId(Long roleId) {
        return roleCache.getRolePermissionsCache(roleId);
    }

    @Override
    public void existsRoleByRoleIdAndNotDelete(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS));
        if(role.getDeletedAt() != null)
            throw new EntityNotFoundException(AuthorizationMessage.Role.DELETED);
    }
}
