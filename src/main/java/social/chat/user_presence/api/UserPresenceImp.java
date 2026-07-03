package social.chat.user_presence.api;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface UserPresenceImp {
    void saveBatchUserPresence();
    void saveAllUserPresence();

    void userOnline(Long userId);
    void userOffline(Long userId);
}
