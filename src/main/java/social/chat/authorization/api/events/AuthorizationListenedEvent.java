package social.chat.authorization.api.events;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.modulith.NamedInterface;
import org.springframework.modulith.events.ApplicationModuleListener;
import org.springframework.stereotype.Component;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.repository.RoleRepository;
import social.chat.user.api.UserImp;

@NamedInterface
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationListenedEvent {
    RoleRepository roleRepository;
    UserImp userImp;

    @ApplicationModuleListener
    public void updateRoleDelete(AuthorizationRoleIdRegisteredEvent event){
        roleRepository.findByRoleName(RoleDefault.USER.name())
                .ifPresent(roleUser -> userImp
                        .updateUserRoleToRole(event.roleId(), roleUser.getRoleId()));
    }
}
