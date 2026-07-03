package social.chat.profile.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;
import org.jspecify.annotations.Nullable;
import social.chat.profile.internal.enums.Gender;
import social.chat.shared.common.BaseEntity;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "profiles")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Profile extends BaseEntity {
    @Override
    public @Nullable Long getId() {
        return this.userId;
    }

    @Id
    @Column(name = "user_id")
    Long userId;

    @Nationalized
    @Column(name = "full_name", length = 125)
    String fullName;

    @Column(name = "username", length = 125)
    String username;

    @Column(name = "avatar_url", length = 500)
    String avatarUrl;

    @Enumerated(EnumType.STRING)
    Gender gender;

    LocalDate birthday;

    Boolean updated;

    @Column(name = "updated_at")
    @UpdateTimestamp
    Instant updatedAt;
}
