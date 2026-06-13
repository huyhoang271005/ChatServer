package social.chat.authentication.internal.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FirebaseConfig {

    @Value("${FIREBASE_CONFIG_JSON}")
    String firebaseConfigJson;

    @PostConstruct
    public void init() {
        try {

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(
                                new ByteArrayInputStream(firebaseConfigJson.getBytes(StandardCharsets.UTF_8))
                        ))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase Admin SDK initialized");
            } else {
                log.info("Firebase Admin SDK not initialize because firebase config has been initialized");
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
    }
}
