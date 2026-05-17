package social.chat.profile.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.profile.internal.entity.Profile;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
}