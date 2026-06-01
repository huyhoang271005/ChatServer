package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import social.chat.authentication.api.dto.SessionDto;
import social.chat.authentication.internal.cache.SessionCache;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.mapper.SessionMapper;
import social.chat.authentication.internal.repository.SessionRepository;
import social.chat.shared.common.GlobalMessage;
import social.chat.shared.dto.Response;
import social.chat.shared.dto.ResponseList;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SessionService {
    SessionRepository sessionRepository;
    SessionMapper sessionMapper;
    SessionCache sessionCache;

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
        sessionCache.evictCacheSession(sessionId, userId);
        return Response.success(
                GlobalMessage.Success.DELETED,
                null
        );
    }
}
