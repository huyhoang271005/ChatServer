package social.chat.message.internal.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.message.internal.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {
    @Query("""
            select m.messageId
            from Message m
            where m.conversationId = :conversationId
            and (:lastId is null or m.messageId < :lastId)
            order by m.messageId desc
            """)
    Slice<Long> findByConversationId(Long conversationId, Long lastId, Pageable pageable);

    Integer deleteByConversationId(Long conversationId);
}