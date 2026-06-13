package social.chat.message.internal;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.Nationalized;
import social.chat.message.api.dto.MessageType;

import java.time.Instant;

@Table(name = "messages", indexes = {
        @Index(name = "idx_message_conversation_and_id", columnList = "conversation_id, message_id DESC")
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Message {
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

    @Column(name = "file_id", length = 500)
    String fileId;

    @Column(name = "created_at")
    Instant createdAt;
}
