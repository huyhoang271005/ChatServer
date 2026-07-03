package social.chat.user_presence.internal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import social.chat.shared.common.BaseEntity;

import java.time.Instant;

@Table(name = "user_presences")
@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserPresence extends BaseEntity {
    @Override
    public @Nullable Long getId() {
        return this.userId;
    }

    @Id
    @Column(name = "user_id")
    Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_presence_status", length = 125)
    UserPresenceStatus userPresenceStatus;

    @Column(name = "last_online")
    Instant lastOnline;
}
