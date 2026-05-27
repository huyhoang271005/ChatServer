package social.chat.user.api;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface UserImp {
    void checkUser(Long userId);
    Long getAndCreateUser();
    Long getRoleIdAndCheckAccountStatus(Long userId);
    void updateAccountStatusFromPendingToActive(Long userId);
    void updateUserRoleToRole(Long oldRoleId, Long newRoleId);
    void updateInactiveToPendingProfile(Long userId);
    void updatePasswordHash(Long userId, String newPassword);
    boolean checkPassword(Long userId, String password);
    boolean checkUpdateProfile(Long userId);
    boolean isInactive(Long userId);
    void hardDeleteUser();
}
