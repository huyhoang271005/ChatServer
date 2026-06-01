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

    @Cacheable(cacheNames = "session", key = "#sessionId")
    @Transactional(readOnly = true)
    public SessionCacheDto getCacheSession(Long sessionId) {
        log.info("Cached session for session {}", sessionId);
        return sessionRepository.findById(sessionId)
                .map(sessionMapper::toSessionCacheDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        AuthenticationMessage.Session.NOT_EXISTS
                ));
    }

    @CachePut(cacheNames = "session", key = "#sessionId")
    public SessionCacheDto putCacheSession(Long sessionId, SessionCacheDto sessionCacheDto) {
        log.info("Updated cache for session {}", sessionId);
        return  sessionCacheDto;
    }

    @CacheEvict(cacheNames = "session", key = "#sessionId")
    @Transactional
    public void evictCacheSession(Long sessionId, Long userId) {
        log.info("Removed cache for session {}", sessionId);
        int sessionDeleted = sessionRepository.deleteByUserIdAndSessionId(userId, sessionId);
        log.info("{} session deleted", sessionDeleted);
    }
}
