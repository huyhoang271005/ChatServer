package social.chat.user.internal.cronjob;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.events.AuthUserIdsRegisteredEvent;
import social.chat.profile.api.events.ProfileUserIdsRegisteredEvent;
import social.chat.user.api.dto.UserInfo;
import social.chat.user.internal.AccountStatus;
import social.chat.user.internal.UserCache;
import social.chat.user.internal.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserCronjob {
    UserRepository userRepository;
    ApplicationEventPublisher applicationEventPublisher;
    UserCronjobProperties userCronjobProperties;
    UserCache userCache;

    @Scheduled(cron = "#{@userCronjobProperties.unbannedUserCron}")
    @Transactional
    public void updateAccountStatusBanned() {
        List<UserInfo> userInfos = userRepository.findUserIdsNeedUnbanned(Instant.now());
        List<Long> userIds = userInfos.stream()
                .map(UserInfo::getUserId)
                .toList();
        int accountUnbanned = userRepository.unbannedByUserIds(userIds);
        log.info("{} account unbanned", accountUnbanned);
        userInfos.forEach(userInfo -> userCache.updateUserCache(userInfo.getUserId(), userInfo.getRoleId(),
                AccountStatus.ACTIVE, null, false));
    }

    @Scheduled(cron = "#{@userCronjobProperties.cleanupUserCron}")
    @Transactional
    public void hardDeleteUser() {
        List<Long> userIds = userRepository.findUserIdsExpired(Instant.now()
                .minus(userCronjobProperties.getDaysToKeepDeletedUser(), ChronoUnit.DAYS));
        userRepository.deleteAllById(userIds);
        ProfileUserIdsRegisteredEvent profileUserIdsRegisteredEvent = new ProfileUserIdsRegisteredEvent(userIds);
        applicationEventPublisher.publishEvent(profileUserIdsRegisteredEvent);
        AuthUserIdsRegisteredEvent authUserIdsRegisteredEvent = new AuthUserIdsRegisteredEvent(userIds);
        applicationEventPublisher.publishEvent(authUserIdsRegisteredEvent);
        log.info("{} user deleted by scheduled", userIds.size());
    }
}
