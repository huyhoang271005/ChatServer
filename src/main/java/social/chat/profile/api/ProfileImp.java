package social.chat.profile.api;

import org.springframework.modulith.NamedInterface;
import social.chat.profile.api.dto.EmailDto;
import social.chat.profile.api.dto.ProfileInfo;

import java.util.List;

@NamedInterface
public interface ProfileImp {
    boolean existsEmailByEmailName(String email);
    boolean existsProfileByUserId(Long userId);
    void createEmail(String email, Long userId, boolean isVerified);
    String getFullName(Long userId);
    EmailDto getUserByEmail(String emailName);
    void verifiedEmail(Long emailId);
    Long getUserIdByEmail(String emailName);
    boolean getUpdated(Long userId);
    void deleteEmailAndProfileByUserIds(List<Long> userIds);
    List<ProfileInfo> getShortProfiles(List<Long> userIds);
}
