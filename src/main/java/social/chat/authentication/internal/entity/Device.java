package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

import java.util.List;

@Table(name = "devices")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Device extends BaseEntity {
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

    String location;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Session> sessions;

    @OneToMany(mappedBy = "device", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Token> tokens;

    @Override
    public @Nullable Long getId() {
        return this.deviceId;
    }
}
