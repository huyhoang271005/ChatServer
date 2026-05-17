package social.chat.profile.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import social.chat.config.generateId.GenerateId;

import java.time.Instant;

@Entity
@Table(name = "emails", indexes = {
        @Index(name = "idx_emails_email", columnList = "email"),
        @Index(name = "idx_emails_created_at", columnList = "created_at")
})
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Email {
    @Id
    @GenerateId
    @Column(name = "email_id")
    Long emailId;

    @Column(columnDefinition = "VARCHAR(125)")
    String emailName;

    Boolean verified;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;
}
