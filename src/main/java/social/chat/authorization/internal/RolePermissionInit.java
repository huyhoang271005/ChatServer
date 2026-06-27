package social.chat.authorization.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.internal.entity.Permission;
import social.chat.authorization.internal.entity.Role;
import social.chat.authorization.internal.enums.PermissionName;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.repository.PermissionRepository;
import social.chat.authorization.internal.repository.RoleRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionInit implements ApplicationRunner {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;

    @Transactional
    public void init() {
        List<Permission> existingPermissions = permissionRepository.findAll();
        Set<String> permissionNames = existingPermissions.stream()
                .map(Permission::getPermissionName)
                .collect(Collectors.toSet());

        // 2. Kiểm tra và gom các quyền mới chưa có trong DB
        List<Permission> newPermissions = new ArrayList<>();
        for (PermissionName permissionName : PermissionName.values()) {
            if (!permissionNames.contains(permissionName.name())) {
                newPermissions.add(Permission.builder()
                        .permissionName(permissionName.name())
                        .build());
            }
        }

        if (!newPermissions.isEmpty()) {
            List<Permission> savedNewPermissions = permissionRepository.saveAll(newPermissions);
            existingPermissions.addAll(savedNewPermissions); // Gộp quyền mới vào danh sách tổng
        }

        List<Role> roles = roleRepository.findAllRolesWithPermissions();
        // 4. Khởi tạo hoặc lấy ra Role ADMIN
        roles.stream()
                .filter(role -> role.getRoleName().equals(RoleDefault.ADMIN.name()))
                .findAny()
                .ifPresentOrElse(role -> {
                            List<String> adminPermissionNames = role.getRolePermissions()
                                    .stream()
                                    .map(rolePermission -> rolePermission.getPermission().getPermissionName())
                                    .toList();
                    List<Permission> adminPermissionsNew = existingPermissions.stream()
                            .filter(permission -> !adminPermissionNames.contains(permission.getPermissionName()))
                            .toList();
                    if(!adminPermissionsNew.isEmpty()) {
                        role.addRolePermission(adminPermissionsNew);
                        roleRepository.save(role);
                    }
                        },
                        () -> {
                            Role role = Role.builder()
                                    .roleName(RoleDefault.ADMIN.name())
                                    .build();
                            role.addRolePermission(existingPermissions);
                            roleRepository.save(role);
                        });

        roles.stream()
                .filter(role -> role.getRoleName().equals(RoleDefault.USER.name()))
                .findAny()
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(RoleDefault.USER.name())
                        .build()));

        log.info("Role Permission initialized");
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) {
        init();
    }
}
