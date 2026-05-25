package social.chat.authorization.api;

import org.springframework.modulith.NamedInterface;

@NamedInterface
public interface AuthorizationImp {
    void hardDeleteRole();
    Long getRoleIdByRoleUser();
}
