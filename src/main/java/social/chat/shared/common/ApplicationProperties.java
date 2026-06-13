package social.chat.shared.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.application")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApplicationProperties {
    String name;
    String backendUrl;
    String frontendUrl;
    String appImage;
    String unknowUserUrl;
}
