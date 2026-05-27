package social.chat.authorization.internal;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.internal.enums.PermissionName;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.entity.Permission;
import social.chat.authorization.internal.entity.Role;
import social.chat.authorization.internal.entity.RolePermission;
import social.chat.authorization.internal.repository.PermissionRepository;
import social.chat.authorization.internal.repository.RoleRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionInit {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    @PostConstruct
    @Transactional
    public void init() {
        List<Permission> permissions = new ArrayList<>();
        Set<String> permissionNames = permissionRepository.findAll()
                .stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());
        for(PermissionName permissionName : PermissionName.values()) {
            if(!permissionNames.contains(permissionName.name())){
                permissions.add(Permission.builder()
                        .permissionName(permissionName.name())
                        .build());
            }
        }
        if(!permissions.isEmpty()) {
            permissionRepository.saveAll(permissions);
        }

        List<Role> roles = new ArrayList<>();
        Set<String> roleNames = roleRepository.findAll()
                .stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());
        for (RoleDefault roleName : RoleDefault.values()) {
            if(!roleNames.contains(roleName.name())){
                Role role = Role.builder()
                        .roleName(roleName.name())
                        .build();
                switch (roleName) {
                    case ADMIN -> role.setRolePermissions(permissions.stream()
                            .map(permission -> RolePermission.builder()
                                    .permission(permission)
                                    .role(role)
                                    .build())
                            .toList());
                    case USER -> {}
                }
                roles.add(role);
            }
        }
        if(!roles.isEmpty()) {
            roleRepository.saveAll(roles);
        }
        log.info("Role Permission initialized");
    }
}
