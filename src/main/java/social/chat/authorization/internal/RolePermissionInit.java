package social.chat.authorization.internal;

import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.internal.enums.PermissionName;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.entity.Permission;
import social.chat.authorization.internal.entity.Role;
import social.chat.authorization.internal.entity.RolePermission;
import social.chat.authorization.internal.repository.PermissionRepository;
import social.chat.authorization.internal.repository.RolePermissionRepository;
import social.chat.authorization.internal.repository.RoleRepository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RolePermissionInit implements ApplicationRunner {
    RoleRepository roleRepository;
    PermissionRepository permissionRepository;
    RolePermissionRepository rolePermissionRepository;

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

        // 3. Nếu có quyền mới, lưu vào DB và gộp chung vào danh sách tổng
        if (!newPermissions.isEmpty()) {
            List<Permission> savedNewPermissions = permissionRepository.saveAll(newPermissions);
            existingPermissions.addAll(savedNewPermissions); // Gộp quyền mới vào danh sách tổng
        }

        // 4. Khởi tạo hoặc lấy ra Role ADMIN
        Role roleAdmin = roleRepository.findByRoleName(RoleDefault.ADMIN.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(RoleDefault.ADMIN.name())
                        .build()));

        // 5. Xóa sạch liên kết cũ của ADMIN
        rolePermissionRepository.deleteByRole(roleAdmin);

        //  CỨU CÁNH Ở ĐÂY: Dùng danh sách TỔNG (existingPermissions) chứa TẤT CẢ các quyền để lưu
        roleAdmin.addRolePermission(existingPermissions);
        roleRepository.save(roleAdmin);

        // 6. Khởi tạo Role USER nếu chưa có
        Role roleUser = roleRepository.findByRoleName(RoleDefault.USER.name())
                .orElseGet(() -> roleRepository.save(Role.builder()
                        .roleName(RoleDefault.USER.name())
                        .build()));
        rolePermissionRepository.deleteByRole(roleUser);

        log.info("Role Permission initialized");
    }

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        init();
    }
}
