package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import social.chat.authentication.internal.entity.User;

import java.time.Instant;
import java.util.List;

public interface UserRepository extends JpaRepository<User, Long> {
    @Modifying
    @Query("""
            update User u
            set u.deletedAt = :deletedAt
            where u.userId in :userIds
            """)
    void softDelete(Instant deletedAt, List<Long> userIds);
}