package social.chat.user_presence.api.event;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import social.chat.user_presence.api.UserPresenceImp;

import java.security.Principal;

@NamedInterface
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPresenceListenedEvent {
    UserPresenceImp userPresenceImp;

    @EventListener
    public void userOnline(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if(user == null) return;
        Long userId = Long.parseLong(user.getName());
        userPresenceImp.userOnline(userId);
    }

    @EventListener
    public void userOffline(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if(user == null) return;
        Long userId = Long.parseLong(user.getName());
        userPresenceImp.userOffline(userId);
    }

    @ApplicationModuleListener
    public void saveBatchUserPresence(SaveAllUserPresence event) {
        userPresenceImp.saveBatchUserPresence();
    }

    @ApplicationModuleListener
    public void saveAllUserPresence(SaveBatchUserPresence event) {
        userPresenceImp.saveAllUserPresence();
    }
}
