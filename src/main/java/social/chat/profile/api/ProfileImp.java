package social.chat.profile.api;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.modulith.NamedInterface;
import social.chat.profile.api.dto.EmailResponse;

import java.util.List;

@NamedInterface
public interface ProfileImp {
    boolean existsEmailByEmailName(String email);
    boolean existsProfileByUserId(Long userId);
    void createEmail(String email, Long userId, boolean isVerified);
    String getFullName(Long userId);
    EmailResponse getUserByEmail(String emailName);
    void verifiedEmail(Long emailId);
    Long getUserIdByEmail(String emailName);
    boolean getUpdated(Long userId);
    void deleteEmailAndProfileByUserIds(List<Long> userIds);
}
