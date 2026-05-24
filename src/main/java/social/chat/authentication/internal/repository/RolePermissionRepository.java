package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.authentication.internal.entity.Permission;
import social.chat.authentication.internal.entity.Role;
import social.chat.authentication.internal.entity.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    boolean existsByRoleAndPermission(Role role, Permission permission);
}