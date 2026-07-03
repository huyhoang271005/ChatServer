package social.chat.shared.storage.api.events;

import java.util.Collection;

public record CloudStorageDeleteEvent(
        Collection<String> fileUrls
) {
}
