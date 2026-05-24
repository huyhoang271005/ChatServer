package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.authentication.internal.entity.Verification;
import social.chat.authentication.internal.enums.AccountStatus;

import java.util.Optional;
import java.util.UUID;

public interface VerificationRepository extends JpaRepository<Verification, Long> {
}