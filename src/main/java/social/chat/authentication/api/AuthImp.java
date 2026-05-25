package social.chat.authentication.api;

import org.springframework.modulith.NamedInterface;
import social.chat.authentication.api.dto.TokenDto;

@NamedInterface
public interface AuthImp {
    void checkUser(Long userId);
    TokenDto generateToken(Long userId, Long sessionId);
    void updateAccountStatusFromPendingToActive(Long userId);
    void expiredVerification();
    void hardDeleteVerification();
    void hardDeleteRole();
    void hardDeleteUser();
}
