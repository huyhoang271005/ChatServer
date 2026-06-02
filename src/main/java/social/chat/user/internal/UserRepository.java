package social.chat.user.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import social.chat.user.api.dto.UserInfo;

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

    @Query("""
            select u.userId
            from User u
            where u.roleId = :roleId
            """)
    List<Long> getUserIdsByRoleId(Long roleId);

    @Modifying
    @Query("""
            update User u
            set u.roleId = :roleId
            where u.userId in :userIds
            """)
    int updateRoleIdByUserIdIn(List<Long> userIds, Long roleId);

    @Query("""
            select u.userId as userId, u.roleId as roleId
            from User u
            where u.accountStatus = AccountStatus.BANNED and u.expireAt < :timeNow
            """)
    List<UserInfo> findUserIdsNeedUnbanned(Instant timeNow);

    @Modifying(clearAutomatically = true)
    @Query("""
            update User u
            set u.accountStatus = AccountStatus.ACTIVE, u.expireAt = null
            where u.userId in :userIds
            """)
    int unbannedByUserIds(List<Long> userIds);
}