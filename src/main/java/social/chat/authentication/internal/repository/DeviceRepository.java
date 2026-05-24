package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.authentication.internal.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {
}