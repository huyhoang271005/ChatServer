package social.chat.verification.api;

import org.springframework.modulith.NamedInterface;

import java.util.List;

@NamedInterface
public interface VerificationImp {
    void deleteBySessionIds(List<Long> sessionIds);
    void sendEmailVerification(String toEmail,
                               String title,
                               String fullName,
                               String activity,
                               String verificationUrl,
                               String timeExpire);
    void expiredVerificationCronjob();
    void hardDeleteVerificationCronjob();
}
