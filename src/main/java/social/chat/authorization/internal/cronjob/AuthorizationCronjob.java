package social.chat.authorization.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authorization.internal.repository.RoleRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthorizationCronjob {
    RoleRepository roleRepository;
    AuthorizationCronjobProperties authorizationCronjobProperties;

    @Scheduled(cron = "#{@authorizationCronjobProperties.cleanupRoleCron}")
    @Transactional
    public void hardDeleteRole() {
        int roleDeleted = roleRepository.deleteRolesWithTimeExpired(Instant.now()
                .minus(authorizationCronjobProperties.getDaysToKeepDeletedRole(), ChronoUnit.DAYS));
        log.info("{} role deleted by scheduled", roleDeleted);
    }
}
