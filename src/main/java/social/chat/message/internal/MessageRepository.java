package social.chat.message.internal;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
            select m
            from Message m
            where m.conversationId = :conversationId
            and (:lastId is null or m.messageId < :lastId)
            order by m.messageId desc
            """)
    Slice<Message> findByConversationId(Long conversationId, Long lastId, Pageable pageable);
}