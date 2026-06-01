package social.chat.profile.internal.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.profile.internal.entity.Email;

import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {
    boolean existsByEmailName(String email);

    void deleteByUserIdIn(List<Long> userIds);

    Optional<Email> findByEmailName(String emailName);

    List<Email> findByUserId(Long userId);

    @Query("""
            select e.userId
            from Email e
            where (:emailName is null or e.emailName like concat('%', :emailName, '%'))
            and (:lastId is null or e.userId < :lastId)
            """)
    Slice<Long> findUserIdsByEmailName(String emailName, Long lastId, Pageable pageable);
}