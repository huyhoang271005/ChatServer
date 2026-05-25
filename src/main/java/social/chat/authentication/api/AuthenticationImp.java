package social.chat.authentication.api;

import org.springframework.modulith.NamedInterface;
import social.chat.authentication.api.dto.TokenDto;

@NamedInterface
public interface AuthenticationImp {
    void checkUser(Long userId);
    TokenDto generateToken(Long userId, Long sessionId);
    void updateAccountStatusFromPendingToActive(Long userId);
    Long checkAccountStatus(Long userId);
    void updateUserRoleToRole(Long oldRoleId, Long newRoleId);
    void expiredVerification();
    void hardDeleteVerification();
    void hardDeleteUser();
}
