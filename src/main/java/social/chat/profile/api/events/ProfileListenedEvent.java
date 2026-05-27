package social.chat.profile.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.profile.internal.repository.EmailRepository;
import social.chat.profile.internal.repository.ProfileRepository;

@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProfileListenedEvent {
    ProfileRepository profileRepository;
    EmailRepository emailRepository;

    @EventListener
    @Transactional
    public void handleProfileRegisteredEvent(ProfileUserIdsRegisteredEvent event) {
        profileRepository.deleteAllById(event.userIds());
        emailRepository.deleteByUserIdIn(event.userIds());
    }
}
