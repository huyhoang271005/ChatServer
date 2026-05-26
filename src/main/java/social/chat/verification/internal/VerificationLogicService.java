package social.chat.verification.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.exception.ConflictException;
import social.chat.verification.api.VerificationImp;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationLogicService implements VerificationImp {
    VerificationRepository verificationRepository;

    @Override
    public void hardDeleteBySessionIds(List<Long> sessionIds) {
        verificationRepository.deleteBySessionIdIn(sessionIds);
    }

    @Override
    @Transactional
    public void expiredVerification() {
        verificationRepository.expireVerificationPending(Instant.now());
    }

    @Override
    @Transactional
    public void hardDeleteVerification() {
        throw new ConflictException("Update soon");
    }
}
