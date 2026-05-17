package social.chat.profile.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.UpdateTimestamp;
import social.chat.profile.api.dto.Gender;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile {
    @Id
    @Column(name = "user_id")
    Long userId;

    @Column(name = "full_name", columnDefinition = "NVARCHAR(50)")
    String fullName;

    @Column(name = "username", columnDefinition = "VARCHAR(50)")
    String username;

    @Column(name = "avatar_id", columnDefinition = "VARCHAR(125)")
    String avatarId;

    @Column(name = "avatar_url", columnDefinition = "VARCHAR(MAX)")
    String avatarUrl;

    @Enumerated(EnumType.STRING)
    Gender gender;

    LocalDate birthday;

    @Column(name = "updated_at")
    @UpdateTimestamp
    Instant updatedAt;
}
