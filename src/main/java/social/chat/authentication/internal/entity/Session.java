package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;

@Table(name = "sessions", indexes = {
        @Index(name = "idx_session_user", columnList = "user_id, session_id desc"),
        @Index(name = "idx_device_user", columnList = "user_id, device_id"),
        @Index(name = "idx_last_login_session_id", columnList = "last_login desc, session_id")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Session extends BaseEntity {
    @Id
    @GenerateId
    @Column(name = "session_id")
    Long sessionId;

    Boolean validated;

    Boolean revoked;

    @Column(name = "ip_address", length = 50)
    String ipAddress;

    @Column(name = "last_login")
    Instant lastLogin;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @Column(name = "user_id")
    Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    Device device;

    @Override
    public @Nullable Long getId() {
        return this.sessionId;
    }
}
