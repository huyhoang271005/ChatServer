package social.chat.authorization.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.authorization.internal.entity.Permission;
import social.chat.authorization.internal.entity.Role;
import social.chat.authorization.internal.entity.RolePermission;

public interface RolePermissionRepository extends JpaRepository<RolePermission, Long> {
    void deleteByRole(Role role);
}