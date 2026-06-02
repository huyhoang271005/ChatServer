package social.chat.authentication.internal.cache;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.SessionCacheDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.mapper.SessionMapper;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.shared.exception.EntityNotFoundException;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionCache {
    SessionRepository sessionRepository;
    SessionMapper sessionMapper;

    @Cacheable(cacheNames = "sessions", key = "#sessionId")
    @Transactional(readOnly = true)
    public SessionCacheDto getCacheSession(Long sessionId) {
        log.info("Cached session for session {}", sessionId);
        return sessionRepository.findById(sessionId)
                .map(sessionMapper::toSessionCacheDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        AuthenticationMessage.Session.NOT_EXISTS
                ));
    }

    @CachePut(cacheNames = "sessions", key = "#sessionId")
    public SessionCacheDto putCacheSession(Long sessionId, boolean revoked, String oldIpAddress,
                                           String newIpAddress, String location, boolean saveDb) {
        log.info("Updated cache for session {}", sessionId);
        if(saveDb && !oldIpAddress.equals(newIpAddress)) {
            Session session = sessionRepository.findById(sessionId)
                    .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS));
            session.setIpAddress(newIpAddress);
            session.setRevoked(revoked);
            session.setLocation(location);
        }
        return  SessionCacheDto.builder()
                .revoked(revoked)
                .ipAddress(newIpAddress)
                .build();
    }

    @CacheEvict(cacheNames = "sessions", key = "#sessionId")
    @Transactional
    public void evictCacheSession(Long sessionId, Long userId, boolean saveDb) {
        log.info("Removed cache for session {}", sessionId);
        if(saveDb && userId != null) {
            int sessionDeleted = sessionRepository.deleteByUserIdAndSessionId(userId, sessionId);
            log.info("{} session deleted", sessionDeleted);
        }
    }
}
