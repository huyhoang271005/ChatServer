package social.chat.conversation.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;
import org.jspecify.annotations.Nullable;
import social.chat.conversation.internal.ConversationRole;
import social.chat.message.api.dto.MessageType;
import social.chat.shared.common.BaseEntity;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(name = "conversations", indexes = {
        @Index(name = "idx_updated_at_conversaton_id", columnList = "updated_at desc")
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation extends BaseEntity {
    @Id
    @GenerateId
    @Column(name = "conversation_id")
    Long conversationId;

    @Nationalized
    @Column(length = 125)
    String title;

    @Column(name = "conversation_avatar_url", length = 500)
    String conversationAvatarUrl;

    @Column(name = "is_group")
    boolean group;

    @Column(name = "last_sender_id")
    Long lastSenderId;

    @Nationalized
    @Column(name = "last_message_text", length = 1000)
    String lastMessageText;

    @Column(name = "last_message_type")
    @Enumerated(EnumType.STRING)
    MessageType lastMessageType;

    @Column(name = "last_message_time")
    Instant lastMessageTime;

    @Column(name = "last_message_id")
    Long lastMessageId;

    @Column(name = "last_message_revoked")
    Boolean lastMessageRevoked;

    @Column(name = "created_by")
    Long createdBy;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "roles_can_chat", length = 125)
    List<ConversationRole> rolesCanChat;

    @Column(name = "created_at")
    Instant createdAt;

    @Column(name = "updated_at")
    Instant updatedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "conversation")
    List<UserConversation> userConversations;

    public List<UserConversation> addUserConversations(List<Long> userIds, ConversationRole role){
        if(this.userConversations == null){
            this.userConversations = new ArrayList<>();
        }
        for(Long userId : userIds){
            UserConversation userConversation = UserConversation.builder()
                    .conversation(this)
                    .userId(userId)
                    .conversationRole(role)
                    .unreadMessage(0)
                    .joinedAt(Instant.now())
                    .build();
            this.userConversations.add(userConversation);
        }
        return this.userConversations;
    }

    @Override
    public @Nullable Long getId() {
        return this.conversationId;
    }
}
