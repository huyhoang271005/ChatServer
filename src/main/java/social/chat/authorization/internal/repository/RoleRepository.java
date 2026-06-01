package social.chat.authorization.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import social.chat.authorization.internal.entity.Role;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(String roleName);
    boolean existsByRoleName(String roleName);
    @Query("""
            select distinct r
            from Role r
            left join fetch r.rolePermissions rp
            left join fetch rp.permission p
            """)
    List<Role> findAllRolesWithPermissions();

    @Modifying(clearAutomatically = true)
    @Query("""
            delete
            from Role r
            where r.deletedAt < :timeExpired
            """)
    int deleteRolesWithTimeExpired(Instant timeExpired);
}