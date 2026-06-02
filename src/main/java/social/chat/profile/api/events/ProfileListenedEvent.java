package social.chat.profile.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.profile.api.ProfileImp;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileListenedEvent {
    ProfileImp profileImp;

    @EventListener
    @Transactional
    public void deleteProfileAndEmail(ProfileAndEmailDeleteRegisteredEvent event) {
        profileImp.deleteEmailAndProfileByUserIds(event.userIds());
    }
}
