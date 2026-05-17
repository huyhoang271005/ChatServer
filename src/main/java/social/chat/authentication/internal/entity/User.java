package social.chat.authentication.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import social.chat.config.generateId.GenerateId;
import social.chat.authentication.api.dto.AccountStatus;

import java.time.Instant;
import java.util.List;

@Table(name = "users", indexes = {
        @Index(name = "idx_users_role", columnList = "role_id"),
        @Index(name = "idx_users_created_at", columnList = "created_at")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User {
    @Id
    @GenerateId
    @Column(name = "user_id")
    Long userId;

    @Column(name = "password_hash", columnDefinition = "VARCHAR(225)")
    String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    AccountStatus accountStatus;

    @Column(name = "expire_at")
    Instant expireAt;

    @Column(name = "deleted_at")
    Instant deletedAt;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id")
    Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    List<Verification> verifications;
}
