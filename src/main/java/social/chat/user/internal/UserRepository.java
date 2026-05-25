package social.chat.user.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Modifying
    @Query("""
            update User u
            set u.deletedAt = :deletedAt
            where u.userId in :userIds
            """)
    void softDelete(Instant deletedAt, List<Long> userIds);

    @Query("""
            select u.userId
            from User u
            where u.deletedAt < :timeExpired
            """)
    List<Long> findUserIdsExpired(Instant timeExpired);

    @Modifying
    @Query("""
            update User u
            set u.roleId = :newRoleId
            where u.roleId = :oldRoleId
            """)
    void updateRoleId(Long newRoleId, Long oldRoleId);
}