package social.chat.verification.api;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface VerificationImp {
    void expiredVerification();
    void hardDeleteVerification();
}
