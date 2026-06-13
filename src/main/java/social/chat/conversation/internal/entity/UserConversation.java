package social.chat.conversation.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import social.chat.conversation.internal.ConversationRole;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;

@Table(name = "user_conversations", indexes = {
        @Index(name = "idx_user_conversation_user", columnList = "user_id"),
        @Index(name = "idx_user_conversation_conversation", columnList = "conversation_id")
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserConversation {
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
}
