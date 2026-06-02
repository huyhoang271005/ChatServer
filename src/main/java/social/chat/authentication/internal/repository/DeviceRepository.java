package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import social.chat.authentication.internal.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {
    int deleteBySessionsIsEmpty();
}