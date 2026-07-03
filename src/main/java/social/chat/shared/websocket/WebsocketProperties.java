package social.chat.shared.websocket;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "app.websocket")
@Component
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class WebsocketProperties {
    String endpoint;
    String broadcastPath;
    String userPath;
    String userDestinationPrefix;
    List<String> appPrefixes;
}
