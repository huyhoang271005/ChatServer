package social.chat.verification.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.verification.internal.VerificationRepository;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationCronjob {
    VerificationRepository verificationRepository;
    VerificationCronjobProperties verificationCronjobProperties;

    @Scheduled(cron = "#{@verificationCronjobProperties.expiredVerificationCron}")
    @Transactional
    public void expiredVerification() {
        int verificationExpired = verificationRepository.expireVerificationPending(Instant.now());
        log.info("{} verification expired by scheduled", verificationExpired);
    }

    @Scheduled(cron = "#{@verificationCronjobProperties.cleanupVerificationCron}")
    @Transactional
    public void hardDeleteVerification() {
        int verificationDeleted = verificationRepository
                .deleteOldVerifications(verificationCronjobProperties.getVerificationToKeep());
        log.info("{} verification deleted by scheduled", verificationDeleted);
    }
}
