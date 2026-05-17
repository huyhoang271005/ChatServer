package social.chat.profile.api;

import org.springframework.modulith.NamedInterface;
import social.chat.profile.api.dto.EmailResponse;

import java.util.List;

@NamedInterface
public interface ProfileImp {
    void deleteProfileAndEmails(List<Long> userIds);
    boolean existsByEmail(String email);
    void createEmail(String email, Long userId, boolean isVerified);
    String getFullName(Long userId);
    EmailResponse getUserByEmail(String emailName);
    void verifiedEmail(String emailName);
}
