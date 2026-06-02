package social.chat.authentication.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.authentication.api.AuthenticationImp;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationListenedEvent {
    AuthenticationImp authenticationImp;

    @ApplicationModuleListener
    public void deleteSessionByUserIds(AuthenticationDeleteSessionByUserIdsRegisteredEvent authenticationDeleteSessionByUserIdsRegisteredEvent){
        authenticationImp.deleteSessionByUserIds(authenticationDeleteSessionByUserIdsRegisteredEvent.userIds());
    }

    @ApplicationModuleListener
    public void revokedSession(AuthenticationRevokedSessionRegisteredEvent event){
        authenticationImp.revokedSessionExpiredCron();
    }

    @ApplicationModuleListener
    public void cleanupDevice(AuthenticationCleanupDeviceRegisteredEvent event){
        authenticationImp.cleanupDeviceCron();
    }

    @ApplicationModuleListener
    public void cleanupSession(AuthenticationCleanupSessionRegisteredEvent event){
        authenticationImp.cleanupSessionCron();
    }
}
