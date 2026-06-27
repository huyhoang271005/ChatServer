package social.chat.cloudinary.api.events;

import java.util.List;

public record CloudinaryDeleteEvent(
        List<String> publicIds
) {
}
