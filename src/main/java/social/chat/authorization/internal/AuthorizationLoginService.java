package social.chat.authorization.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.api.AuthorizationImp;
import social.chat.authorization.internal.enums.RoleDefault;
import social.chat.authorization.internal.repository.RoleRepository;
import social.chat.shared.exception.EntityNotFoundException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationLoginService implements AuthorizationImp {
    RoleRepository roleRepository;

    @Override
    @Transactional
    public void hardDeleteRole() {
        //The role was deleted 7 days ago.
        roleRepository.deleteRolesWithTimeExpired(Instant.now()
                .minus(7, ChronoUnit.DAYS));
    }

    @Override
    @Transactional(readOnly = true)
    public Long getRoleIdByRoleUser() {
        return roleRepository.findByRoleName(RoleDefault.USER.name())
                .orElseThrow(() -> new EntityNotFoundException(AuthorizationMessage.Role.NOT_EXISTS))
                .getRoleId();
    }
}
