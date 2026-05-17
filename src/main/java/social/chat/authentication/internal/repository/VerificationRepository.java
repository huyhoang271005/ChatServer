package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.authentication.internal.entity.Verification;

import java.util.UUID;

public interface VerificationRepository extends JpaRepository<Verification, UUID> {
}