package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.entity.Session;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByDeviceAndUserId(Device device, Long userId);
    @Query("""
            select s.userId
            from Verification v
            join v.session s
            where v.verificationId = :verificationId
            """)
    Optional<Long> findUserIdByVerificationId(Long verificationId);
}