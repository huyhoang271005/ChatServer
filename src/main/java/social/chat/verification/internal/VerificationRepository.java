package social.chat.verification.internal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import social.chat.verification.internal.enums.VerificationType;

import java.time.Instant;
import java.util.List;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
    @Modifying
    @Query("""
            update Verification v
            set v.verificationStatus = VerificationStatus.CANCELLED
            where v.verificationStatus = VerificationStatus.PENDING
            and v.typeId = :typeId
            and v.expiredAt >= :timeNow
            """)
    void cancelVerificationPending(Long typeId, Instant timeNow);

    @Modifying
    @Query("""
            update Verification v
            set v.verificationStatus = VerificationStatus.EXPIRED
            where v.verificationStatus != VerificationStatus.USED
            and v.expiredAt < :timeNow
            """)
    void expireVerificationPending(Instant timeNow);

    List<Verification> findBySessionIdAndVerificationTypeAndTypeIdAndExpiredAtAfterOrderByCreatedAtDesc(
            Long sessionId, VerificationType verificationType, Long typeId, Instant timeNow
    );

    void deleteBySessionIdIn(List<Long> sessionIds);
}