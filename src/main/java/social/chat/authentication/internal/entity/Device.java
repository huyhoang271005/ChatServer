package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.config.generateId.GenerateId;

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

    @Column(name = "device_name", columnDefinition = "NVARCHAR(125)")
    String deviceName;

    @Column(name = "device_type", columnDefinition = "VARCHAR(50)")
    String deviceType;

    @Column(name = "user_agent", columnDefinition = "VARCHAR(225)")
    String userAgent;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Session> sessions;
}
