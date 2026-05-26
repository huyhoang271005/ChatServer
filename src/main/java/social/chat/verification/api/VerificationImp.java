package social.chat.verification.api;

import org.springframework.modulith.NamedInterface;

import java.util.List;

@NamedInterface
public interface VerificationImp {
    void hardDeleteBySessionIds(List<Long> sessionIds);
    void expiredVerification();
    void hardDeleteVerification();
}
