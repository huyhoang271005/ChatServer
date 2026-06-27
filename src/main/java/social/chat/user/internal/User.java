package social.chat.user.internal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;

@Table(name = "users", indexes = {
        @Index(name = "idx_users_role", columnList = "role_id"),
        @Index(name = "idx_account_status_expire_at", columnList = "account_status, expire_at"),
        @Index(name = "idx_deleted_at", columnList = "deleted_at")
})
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class User extends BaseEntity {
    @Override
    public @Nullable Long getId() {
        return this.userId;
    }

    @Id
    @GenerateId
    @Column(name = "user_id")
    Long userId;

    @Column(name = "password_hash", length = 500)
    String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status")
    AccountStatus accountStatus;

    @Column(name = "expire_at")
    Instant expireAt;

    @Column(name = "deleted_at")
    Instant deletedAt;

    @Column(name = "role_id")
    Long roleId;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;
}
