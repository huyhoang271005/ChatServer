package social.chat.authentication.internal.service;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.PermissionName;
import social.chat.authentication.api.dto.RoleDefault;
import social.chat.authentication.internal.entity.Permission;
import social.chat.authentication.internal.entity.Role;
import social.chat.authentication.internal.entity.RolePermission;
import social.chat.authentication.internal.repository.PermissionRepository;
import social.chat.authentication.internal.repository.RoleRepository;

import java.util.*;
import java.util.stream.Collectors;

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
                if(roleName == RoleDefault.ADMIN){
                    role.setRolePermissions(permissions.stream()
                            .map(permission -> RolePermission.builder()
                                    .permission(permission)
                                    .role(role)
                                    .build())
                            .toList());
                } else if(roleName == RoleDefault.USER){

                }
                roles.add(role);
            }
        }
        if(!roles.isEmpty()) {
            roleRepository.saveAll(roles);
        }
    }
}
