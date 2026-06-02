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
    int cancelVerificationPending(Long typeId, Instant timeNow);

    @Modifying
    @Query("""
            update Verification v
            set v.verificationStatus = VerificationStatus.EXPIRED
            where v.verificationStatus != VerificationStatus.USED
            and v.verificationStatus != VerificationStatus.EXPIRED
            and v.expiredAt < :timeNow
            """)
    int expireVerificationPending(Instant timeNow);

    List<Verification> findBySessionIdAndVerificationTypeAndTypeIdAndExpiredAtAfterOrderByCreatedAtDesc(
            Long sessionId, VerificationType verificationType, Long typeId, Instant timeNow
    );

    int deleteBySessionIdIn(List<Long> sessionIds);

    @Modifying(clearAutomatically = true)
    @Query("""
            delete from Verification v
            where v.verificationId in (
                select sub.id
                from (
                    select vSub.verificationId as id,
                           row_number() over(
                               partition by vSub.sessionId\s
                               order by vSub.verificationId desc
                           ) as rn
                    from Verification vSub
                ) sub
                where sub.rn > :verificationToKeep
                    )
           """)
    int deleteOldVerifications(int verificationToKeep);

    List<Verification> findBySessionId(Long sessionId);
}