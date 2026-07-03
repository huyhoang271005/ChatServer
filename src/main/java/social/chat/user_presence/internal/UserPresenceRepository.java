package social.chat.user_presence.internal;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPresenceRepository extends JpaRepository<UserPresence, Long> {
}