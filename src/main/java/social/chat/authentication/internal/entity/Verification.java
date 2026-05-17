package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import social.chat.authentication.api.dto.VerificationType;
import social.chat.config.generateId.GenerateId;

import java.time.Instant;

@Entity
@Table(name = "verification", indexes = {
        @Index(name = "idx_verification_user", columnList = "user_id")
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
    Long verificationTokenId;

    @Column(name = "type_id")
    Long typeId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    VerificationType verificationType;

    @Column(name = "expired_at")
    Instant expiredAt;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    User user;
}
