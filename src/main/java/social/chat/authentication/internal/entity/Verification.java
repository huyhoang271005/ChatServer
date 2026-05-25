package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import social.chat.authentication.internal.enums.VerificationStatus;
import social.chat.authentication.internal.enums.VerificationType;
import social.chat.config.generateId.GenerateId;

import java.time.Instant;

@Entity
@Table(name = "verification", indexes = {
        @Index(name = "idx_verification_session", columnList = "session_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Verification {
    @Id
    @GenerateId
    @Column(name = "verification_id")
    Long verificationId;

    @Column(name = "type_id")
    Long typeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    VerificationType verificationType;

    @Column(name = "used_at")
    Instant usedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    VerificationStatus verificationStatus;

    @Column(name = "expired_at")
    Instant expiredAt;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    Session session;
}
