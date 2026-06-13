package social.chat.conversation.internal.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.UpdateTimestamp;
import social.chat.conversation.internal.ConversationRole;
import social.chat.message.api.dto.MessageType;
import social.chat.shared.generateId.GenerateId;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Table(name = "conversations", indexes = {
        @Index(name = "idx_conversation_last_message", columnList = "last_message_id")
})
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Conversation {
    @Id
    @GenerateId
    @Column(name = "conversation_id")
    Long conversationId;

    @Nationalized
    @Column(length = 125)
    String title;

    @Column(name = "conversation_avatar", length = 500)
    String conversationAvatar;

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

    @Column(name = "created_at")
    @CreationTimestamp
    Instant createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    Instant updatedAt;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "conversation")
    List<UserConversation> userConversations;

    public void addUserConversations(List<Long> userIds, ConversationRole role){
        if(this.userConversations == null){
            this.userConversations = new ArrayList<>();
        }
        for(Long userId : userIds){
            UserConversation userConversation = UserConversation.builder()
                    .conversation(this)
                    .userId(userId)
                    .conversationRole(role)
                    .unreadMessage(0)
                    .build();
            this.userConversations.add(userConversation);
        }
    }
}
