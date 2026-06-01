package social.chat.authorization.api;

import org.springframework.modulith.NamedInterface;
import social.chat.authorization.api.dto.RolePermissionDto;

@NamedInterface
public interface AuthorizationImp {
    Long getRoleIdByRoleUser();
    RolePermissionDto getRolePermissionByRoleId(Long roleId);
    void existsRoleByRoleIdAndNotDelete(Long roleId);
}
