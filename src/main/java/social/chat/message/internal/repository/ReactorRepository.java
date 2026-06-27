package social.chat.message.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.message.internal.entity.Reactor;

import java.util.List;
import java.util.Optional;

public interface ReactorRepository extends JpaRepository<Reactor, Long> {
    Optional<Reactor> findByUserIdAndMessage_MessageId(Long userId, Long messageId);

    @Query("""
            select r.reactorId
            from Reactor r
            where r.message.messageId = :messageId
            """)
    List<Long> getReactorIdsByUserIdAndMessageId(Long messageId);
}