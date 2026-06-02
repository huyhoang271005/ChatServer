package social.chat.verification.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.verification.api.VerificationImp;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VerificationListenedEvent {
    VerificationImp verificationImp;



    @ApplicationModuleListener
    public void sendEmailVerification(VerificationSendEmailRegisteredEvent event) {
        verificationImp.sendEmailVerification(event.toEmail(), event.title(), event.fullName(),
                event.activity(), event.verificationUrl(), event.timeExpire());
    }

    @ApplicationModuleListener
    public void deleteVerificationBySessionIds(VerificationDeleteBySessionIdsRegisteredEvent verificationDeleteBySessionIdsRegisteredEvent){
        verificationImp.deleteBySessionIds(verificationDeleteBySessionIdsRegisteredEvent.sessionIds());
    }

    @ApplicationModuleListener
    public void expiredVerificationCronjob(VerificationExpiredRegisteredEvent event){
        verificationImp.expiredVerificationCronjob();
    }

    @ApplicationModuleListener
    public void hardDeleteVerificationCronjob(VerificationHardDeleteRegisteredEvent event){
        verificationImp.hardDeleteVerificationCronjob();
    }
}
