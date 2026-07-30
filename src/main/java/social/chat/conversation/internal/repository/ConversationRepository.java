package social.chat.conversation.internal.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.conversation.internal.entity.Conversation;

import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    @Query("""
            select c
            from Conversation c
            join fetch c.userConversations
            where c.conversationId in :conversationIds
            """)
    List<Conversation> findByConversationIds(List<Long> conversationIds);

    @Query("""
            select c.conversationId
            from UserConversation uc
            join uc.conversation c
            where uc.userId = :userId
            and (:lastId is null or c.conversationId < :lastId)
            and (
                        :title is null
                        or (c.group = true and c.title like concat(:title, '%'))
                        or (c.group = false)
                )
            order by c.updatedAt desc
            """)
    Slice<Long> findConversationIdsByUserId(Long userId, Long lastId, String title, Pageable pageable);

    @Query("""
    select count(c) > 0 from Conversation c where c in (
        select uc.conversation
        from UserConversation uc
        where uc.conversation.group = :group
          and uc.userId in :userIds
        group by uc.conversation
        having count(distinct uc.userId) = :#{#userIds.size()})
    """)
    boolean existsByGroupAndAllUsers(Boolean group, List<Long> userIds);
}