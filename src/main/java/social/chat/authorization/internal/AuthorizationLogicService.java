package social.chat.authorization.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.authorization.api.dto.RolePermissionDto;
import social.chat.authorization.internal.cronjob.AuthorizationCronjobProperties;
import social.chat.authorization.internal.entity.Role;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.repository.RoleRepository;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.user.api.events.UserUpdateRoleToRoleRegisteredEvent;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationLogicService implements AuthorizationImp {
    RoleRepository roleRepository;
    RoleCache roleCache;
    ApplicationEventPublisher applicationEventPublisher;
    AuthorizationCronjobProperties authorizationCronjobProperties;

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
    @Transactional(readOnly = true)
    public void existsRoleByRoleIdAndNotDelete(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS));
        if(role.getDeletedAt() != null)
            throw new EntityNotFoundException(AuthorizationMessage.Role.DELETED);
    }

    @Override
    @Transactional
    public void updateRoleToUser(Long oldRoleId) {
        roleRepository.findByRoleName(RoleDefault.USER.name())
                .ifPresent(roleUser -> applicationEventPublisher
                        .publishEvent(new UserUpdateRoleToRoleRegisteredEvent(oldRoleId,
                                roleUser.getRoleId())));
    }

    @Override
    @Transactional
    public void hardDeleteRoleCronjob() {
        int roleDeleted = roleRepository.deleteRolesWithTimeExpired(Instant.now()
                .minus(authorizationCronjobProperties.getDaysToKeepDeletedRole(), ChronoUnit.DAYS));
        log.info("{} role deleted by scheduled", roleDeleted);
    }
}
