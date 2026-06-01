package social.chat.authentication.internal.mapper;

import org.mapstruct.Mapper;
import social.chat.authentication.api.dto.SessionCacheDto;
import social.chat.authentication.api.dto.SessionDto;
import social.chat.authentication.internal.entity.Session;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    List<SessionDto> toSessionDto(List<Session> sessions);
    SessionCacheDto toSessionCacheDto(Session session);
}
