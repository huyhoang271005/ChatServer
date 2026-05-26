package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.entity.Session;

import java.util.List;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByDeviceAndUserId(Device device, Long userId);

    @Query("""
            select s.sessionId
            from Session s
            where s.userId in :userIds
            """)
    List<Long> findSessionIdsByUserIds(List<Long> userIds);
}