package social.chat.authentication.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.verification.api.events.VerificationSessionIdsRegisteredEvent;

import java.util.List;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationListenedEvent {

    SessionRepository sessionRepository;
    ApplicationEventPublisher applicationEventPublisher;

    @ApplicationModuleListener
    public void deleteSessionByUserIds(AuthUserIdsRegisteredEvent authUserIdsRegisteredEvent){
        List<Long> sessionIds = sessionRepository.findSessionIdsByUserIds(authUserIdsRegisteredEvent.userIds());
        sessionRepository.deleteAllById(sessionIds);
        VerificationSessionIdsRegisteredEvent verificationSessionIdsRegisteredEvent =
                new VerificationSessionIdsRegisteredEvent(sessionIds);
        applicationEventPublisher.publishEvent(verificationSessionIdsRegisteredEvent);
    }
}
