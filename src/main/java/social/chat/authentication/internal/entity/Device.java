package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;
import social.chat.shared.generateId.GenerateId;

import java.util.List;

@Table(name = "devices")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Device {
    @Id
    @GenerateId
    @Column(name = "device_id")
    Long deviceId;

    @Nationalized
    @Column(name = "device_name", length = 125)
    String deviceName;

    @Column(name = "device_type", length = 50)
    String deviceType;

    @Column(name = "user_agent")
    String userAgent;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Session> sessions;
}
