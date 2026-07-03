package social.chat.shared.storage.internal;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "cloudflare.r2")
@Component
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CloudStorageProperties {
    String endpoint;
    String accessKey;
    String secretKey;
    String bucketName;
    String bucketUrl;
}
