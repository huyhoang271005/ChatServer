package social.chat.conversation.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.jspecify.annotations.Nullable;
import social.chat.conversation.internal.ConversationRole;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;

@Table(name = "user_conversations", indexes = {
        @Index(name = "idx_user_conversation", columnList = "user_id, conversation_id desc")
}, uniqueConstraints = {
        @UniqueConstraint(name = "uc_user_conversation", columnNames = {"conversation_id", "user_id"})
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserConversation extends BaseEntity {
    @Id
    @GenerateId
    @Column(name = "user_conversation_id")
    Long userConversationId;

    @Column(name = "user_id")
    Long userId;

    @Column(name = "conversation_role")
    @Enumerated(EnumType.STRING)
    ConversationRole conversationRole;

    @Column(name = "last_message_id")
    Long lastMessageId;

    @Column(name = "unread_message")
    Integer unreadMessage;

    @Column(name = "joined_at")
    Instant joinedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id")
    Conversation conversation;

    @Override
    public @Nullable Long getId() {
        return this.userConversationId;
    }
}
