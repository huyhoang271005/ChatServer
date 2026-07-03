package social.chat.user_presence.internal;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import social.chat.shared.websocket.WebsocketEventType;
import social.chat.shared.websocket.WebsocketService;
import social.chat.user_presence.api.UserPresenceDto;
import social.chat.user_presence.api.UserPresenceImp;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class UserPresenceLogic implements UserPresenceImp {
    UserPresenceCache userPresenceCache;
    SimpUserRegistry simpUserRegistry;
    private final WebsocketService websocketService;

    @Override
    public void saveBatchUserPresence() {
        Collection<Long> userIds = userPresenceCache.getBatchUserIdsPending();
        userPresenceCache.saveData(userIds);
    }

    @Override
    public void saveAllUserPresence() {
        Collection<Long> userIds = userPresenceCache.getAllUserIdsPending();
        userPresenceCache.saveData(userIds);
    }

    void updateUserPresenceCache(Long userId, UserPresenceStatus userPresenceStatus, boolean addCount) {
        Instant now = Instant.now();

        UserPresenceDto userPresenceDtoCache = userPresenceCache
                .getPresencesCache(List.of(userId))
                .orElseGet(() -> List.of(new UserPresenceDto(userId, UserPresenceStatus.OFFLINE, now, 0, true)))
                .getFirst();

        int newCount = userPresenceDtoCache.count() + (addCount ? 1 : -1);
        if (newCount < 0) {
            newCount = 0;
        }

        UserPresenceStatus nextStatus;
        if (newCount == 0) {
            nextStatus = UserPresenceStatus.OFFLINE;
        } else {
            nextStatus = addCount ? userPresenceStatus : userPresenceDtoCache.userPresenceStatus();
        }

        UserPresenceDto userPresenceDtoNew = new UserPresenceDto(
                userId,
                nextStatus,
                now,
                newCount,
                userPresenceDtoCache.isNew()
        );

        List<Long> userIds = simpUserRegistry.getUsers()
                .stream()
                .map(simpUser -> Long.parseLong(simpUser.getName()))
                .toList();
        if(userPresenceDtoNew.userPresenceStatus() == UserPresenceStatus.ONLINE &&
            userPresenceDtoNew.count() == 1) {
            userIds.forEach(id -> websocketService.sendMessageToUser(userId, id, null,
                    WebsocketEventType.USER_ONLINE, userPresenceDtoNew));
        }
        else if(userPresenceDtoNew.userPresenceStatus() == UserPresenceStatus.OFFLINE ) {
            userIds.forEach(id -> websocketService.sendMessageToUser(userId, id, null,
                    WebsocketEventType.USER_OFFLINE, userPresenceDtoNew));
        }

        userPresenceCache.updateUserPresence(userId, userPresenceDtoNew);
    }

    @Override
    public void userOnline(Long userId) {
        log.info("User {} has been online", userId);
        updateUserPresenceCache(userId, UserPresenceStatus.ONLINE, true);
    }

    @Override
    public void userOffline(Long userId) {
        log.info("User {} has been offline", userId);
        updateUserPresenceCache(userId, UserPresenceStatus.OFFLINE, false);
    }
}
