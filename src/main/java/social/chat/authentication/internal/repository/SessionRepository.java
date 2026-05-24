package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.entity.User;

import java.util.Optional;

public interface SessionRepository extends JpaRepository<Session, Long> {
    Optional<Session> findByDeviceAndUser(Device device, User user);
}