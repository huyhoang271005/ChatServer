package social.chat.cloudinary.api.events;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@NamedInterface
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudinaryListenedEvent {
    Cloudinary cloudinary;

    @ApplicationModuleListener
    public void deleteImages(CloudinaryRegisteredEvent event) {
        if(event.publicIds() != null && !event.publicIds().isEmpty()){
            try {
                Map<?, ?> result = cloudinary.api().deleteResources(event.publicIds(), ObjectUtils.emptyMap());
                Map<?, ?> deletedMap = (Map<?, ?>) result.get("deleted");
                log.info(deletedMap.toString());
            }
            catch (Exception e) {
                throw new RuntimeException(e.getMessage());
            }
        }
    }
}
