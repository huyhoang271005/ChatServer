package social.chat.message.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Nationalized;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;
import social.chat.message.api.dto.MessageType;
import social.chat.message.internal.ReactionType;
import social.chat.shared.common.BaseEntity;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Table(name = "messages", indexes = {
        @Index(name = "idx_conversation_id", columnList = "conversation_id, message_id desc")
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message extends BaseEntity {
    @Override
    public @Nullable Long getId() {
        return this.messageId;
    }

    @Id
    @Column(name = "message_id")
    Long messageId;

    @Nationalized
    @Column(length = 1000)
    String text;

    @Enumerated(EnumType.STRING)
    MessageType type;

    Boolean revoked;

    @Column(name = "sender_id")
    Long senderId;

    @Column(name = "conversation_id")
    Long conversationId;

    @Column(name = "reply_message_id")
    Long replyMessageId;

    @Column(name = "created_at")
    Instant createdAt;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "reactor_count", length = 125)
    Map<ReactionType, Integer> reactorCount;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "message")
    List<Reactor> reactors;
}
