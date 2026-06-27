package social.chat.message.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;
import social.chat.message.internal.ReactionType;
import social.chat.shared.common.BaseEntity;

import java.util.Map;

@Table(name = "reactors", indexes = {
        @Index(name = "idx_message_id", columnList = "message_id, user_id")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_user_id_message_id", columnNames = {"user_id", "message_id"})
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Reactor extends BaseEntity {
    @Id
    @Column(name = "reactor_id")
    Long reactorId;

    @Column(name = "user_id")
    Long userId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reaction_count", length = 125)
    Map<ReactionType, Integer> reactionCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id")
    Message message;

    @Override
    public @Nullable Long getId() {
        return this.reactorId;
    }
}
