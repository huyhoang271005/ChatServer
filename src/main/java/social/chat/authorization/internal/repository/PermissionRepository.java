package social.chat.authorization.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.authorization.internal.entity.Permission;

public interface PermissionRepository extends JpaRepository<Permission, Long> {
}