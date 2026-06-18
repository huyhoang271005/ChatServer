package social.chat.profile.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.profile.api.dto.ProfileInfo;
import social.chat.profile.internal.entity.Profile;

import java.util.List;

public interface ProfileRepository extends JpaRepository<Profile, Long> {
    @Query("""
            select p.userId, p.fullName, p.username, coalesce(p.avatarUrl, :userUnknowUrl)
            from Profile p
            where p.userId in :userIds
            """)
    List<ProfileInfo> getProfileInfo(List<Long> userIds, String userUnknowUrl);



    boolean existsByUsernameAndUserIdNot(String username, Long userId);
}