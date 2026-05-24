package social.chat.cloudinary.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "cloudinary")
@Component
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudinaryProperties {
    String apiKey;
    String apiSecret;
    String cloudName;
}
