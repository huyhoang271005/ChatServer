package social.chat.conversation.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.conversation.internal.entity.UserConversation;

public interface UserConversationRepository extends JpaRepository<UserConversation, Long> {
}