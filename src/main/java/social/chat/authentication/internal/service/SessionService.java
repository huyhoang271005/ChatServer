package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.SessionDto;
import social.chat.authentication.internal.AuthenticationMessage;
import social.chat.authentication.internal.cache.SessionCache;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.mapper.SessionMapper;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.dto.ResponseList;
import social.chat.shared.exception.EntityNotFoundException;
import social.chat.verification.api.events.VerificationDeleteBySessionIdsRegisteredEvent;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionService {
    SessionRepository sessionRepository;
    SessionMapper sessionMapper;
    SessionCache sessionCache;
    ApplicationEventPublisher applicationEventPublisher;

    @Transactional(readOnly = true)
    public Response<ResponseList<SessionDto>> getSessions(Long userId, Long lastId, Pageable pageable,
                                                          Long sessionId) {
        Slice<Session> sessions = sessionRepository.findByUserIdAndLastId(userId, lastId, pageable);
        List<SessionDto> sessionDtos = sessionMapper.toSessionDto(sessions.getContent());
        sessionDtos.forEach(sessionDto -> sessionDto
                .setMySession(Long.parseLong(sessionDto.getSessionId()) == sessionId));
        return Response.success(
                GlobalMessage.Success.GET,
                new ResponseList<>(
                        sessionDtos,
                        sessions.hasNext()
                )
        );
    }

    @Transactional
    public Response<Void> deleteSession(Long userId, Long sessionId) {
        sessionCache.evictCacheSession(sessionId, userId, true);
        applicationEventPublisher
                .publishEvent(new VerificationDeleteBySessionIdsRegisteredEvent(List.of(sessionId)));
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }

    @Transactional
    public Response<Void> revokedSession(Long userId, Long sessionId) {
        Session session = sessionRepository.findBySessionIdAndUserId(userId, sessionId)
                .orElseThrow(() -> new EntityNotFoundException(AuthenticationMessage.Session.NOT_EXISTS));
        session.setRevoked(true);
        sessionCache.evictCacheSession(sessionId, null, false);
        return Response.success(
                GlobalMessage.Success.UPDATED,
                null);
    }
}
