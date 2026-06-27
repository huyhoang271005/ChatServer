package social.chat.verification.internal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;
import social.chat.verification.internal.enums.VerificationStatus;
import social.chat.verification.internal.enums.VerificationType;

import java.time.Instant;

@Entity
@Table(name = "verification", indexes = {
        @Index(name = "idx_session_id_type_type_id_created_at",
                columnList = "session_id, type, type_id, created_at desc"),
        @Index(name = "idx_type_id_status", columnList = "type_id, status"),
        @Index(name = "idx_expired_at_status", columnList = "expired_at, status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Verification extends BaseEntity {
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

    @Column(name = "session_id")
    Long sessionId;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @Override
    public @Nullable Long getId() {
        return this.verificationId;
    }
}
