package social.chat.cloudinary.api.events;

import java.util.List;

public record CloudinaryRegisteredEvent(
        List<String> publicIds
) {
}
