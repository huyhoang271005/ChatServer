package social.chat.user.api;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface UserImp {
    void checkUser(Long userId);
    Long getAndCreateUser();
    Long getRoleIdAndCheckAccountStatus(Long userId);
    void updateAccountStatusFromPendingToActive(Long userId);
    void updateAccountStatusToInactive(Long userId);
    void updateUserRoleToRole(Long oldRoleId, Long newRoleId);
    void updateInactiveToPendingProfileOrActive(Long userId, boolean profileUpdated);
    void updatePasswordHash(Long userId, String newPassword);
    boolean checkPassword(Long userId, String password);
    boolean isInactive(Long userId);
}
