package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import social.chat.config.generateId.GenerateId;

import java.time.Instant;
import java.util.List;

@Table(name = "sessions", indexes = {
        @Index(name = "idx_session_user", columnList = "user_id"),
        @Index(name = "idx_session_created_at", columnList = "created_at"),
        @Index(name = "idx_session_device", columnList = "device_id")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Session {
    @Id
    @GenerateId
    @Column(name = "session_id")
    Long sessionId;

    @Column(columnDefinition = "NVARCHAR(225)")
    String location;

    Boolean validated;

    Boolean revoked;

    @Column(name = "ip-address", columnDefinition = "VARCHAR(50)")
    String ipAddress;

    @Column(name = "last-login")
    Instant lastLogin;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id")
    Device device;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Token> tokens;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Verification> verifications;
}
