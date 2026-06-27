package social.chat.profile.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;

@Entity
@Table(name = "emails", indexes = {
        @Index(name = "idx_email_name", columnList = "email_name"),
        @Index(name = "idx_user_id", columnList = "user_id"),
        @Index(name = "idx_email_search_pagination", columnList = "email_name, user_id desc")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Email extends BaseEntity {
    @Override
    public @Nullable Long getId() {
        return this.emailId;
    }

    @Id
    @GenerateId
    @Column(name = "email_id")
    Long emailId;

    @Column(name = "email_name", length = 125)
    String emailName;

    Boolean verified;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;
}
