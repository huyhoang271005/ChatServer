package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import social.chat.authentication.internal.entity.User;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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

    @Query("""
            select u
            from Verification v
            join v.session s
            join s.user u
            where v.verificationId = :verificationId
            """)
    Optional<User> findByVerificationId(Long verificationId);

    @Modifying
    @Query("""
            update User u
            set u.roleId = :newRoleId
            where u.roleId = :oldRoleId
            """)
    void updateRoleId(Long newRoleId, Long oldRoleId);
}