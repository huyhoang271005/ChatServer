package social.chat.profile.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.profile.internal.entity.Email;

import java.util.List;
import java.util.Optional;

public interface EmailRepository extends JpaRepository<Email, Long> {
    boolean existsByEmailName(String email);

    void deleteByUserIdIn(List<Long> userIds);

    Optional<Email> findByEmailName(String emailName);
}