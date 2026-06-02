package social.chat.verification.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.shared.common.ApplicationProperties;
import social.chat.shared.emailSender.EmailSenderService;
import social.chat.verification.api.VerificationImp;
import social.chat.verification.internal.cronjob.VerificationCronjobProperties;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationLogicService implements VerificationImp {
    EmailSenderService emailSenderService;
    ApplicationProperties applicationProperties;
    VerificationRepository verificationRepository;
    VerificationCronjobProperties verificationCronjobProperties;

    private void sendEmailVerify(String toEmail, String title, String fullName, String activity,
                                String verificationUrl, String timeExpire) {
        emailSenderService.sendEmail(toEmail, title, "verified", Map.of(
                "appName", applicationProperties.getName(),
                "fullName", fullName,
                "activity", activity,
                "verificationUrl", verificationUrl,
                "timeExpire", timeExpire
        ));
    }

    @Override
    public void deleteBySessionIds(List<Long> sessionIds) {
        int verificationDeleted = verificationRepository.deleteBySessionIdIn(sessionIds);
        log.info("{} verification deleted by session deleted", verificationDeleted);
    }

    @Override
    public void sendEmailVerification(String toEmail, String title, String fullName, String activity,
                                      String verificationUrl, String timeExpire) {
        log.info("has received the event send verified email");
        sendEmailVerify(toEmail, title, fullName,
                activity, verificationUrl, timeExpire);
    }

    @Override
    @Transactional
    public void expiredVerificationCronjob() {
        int verificationExpired = verificationRepository.expireVerificationPending(Instant.now());
        log.info("{} verification expired by scheduled", verificationExpired);
    }

    @Override
    @Transactional
    public void hardDeleteVerificationCronjob() {
        int verificationDeleted = verificationRepository
                .deleteOldVerifications(verificationCronjobProperties.getVerificationToKeep());
        log.info("{} verification deleted by scheduled", verificationDeleted);
    }
}
