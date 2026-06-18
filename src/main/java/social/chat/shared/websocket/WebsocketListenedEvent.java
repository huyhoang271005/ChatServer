package social.chat.shared.websocket;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class WebsocketListenedEvent {

    @EventListener
    public void userOnline(SessionSubscribeEvent event) {

    }

    @EventListener
    public void userOffline(SessionDisconnectEvent event) {

    }
}
