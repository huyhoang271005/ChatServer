package social.chat.shared.storage.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.shared.storage.api.CloudStorageImp;

@Slf4j
@NamedInterface
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CloudStorageListenedEvent {
    CloudStorageImp cloudStorageImp;

    @ApplicationModuleListener
    public void deleteMultipleFiles(CloudStorageDeleteEvent deleteEvent) {
        cloudStorageImp.deleteMultipleFile(deleteEvent.fileUrls());
    }
}
