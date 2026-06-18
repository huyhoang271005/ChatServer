package social.chat.authentication.internal.mapper;

import org.mapstruct.*;
import social.chat.authentication.api.dto.SessionCacheDto;
import social.chat.authentication.api.dto.SessionDto;
import social.chat.authentication.api.dto.SessionValidation;
import social.chat.authentication.api.dto.TokenDto;
import social.chat.authentication.internal.entity.Session;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SessionMapper {
    List<SessionDto> toSessionDto(List<Session> sessions, @Context Long mySessionId);

    @Mapping(target = "mySession", source = "sessionId", qualifiedByName = "checkMySession")
    SessionDto toSessionDto(Session session, @Context Long userId);

    @Named("checkMySession")
    default boolean checkMySession(Long sessionId, @Context Long mySessionId) {
        if(sessionId == null || mySessionId == null) {
            return false;
        }
        return sessionId.equals(mySessionId);
    }

    SessionCacheDto toSessionCacheDto(Session session);

    @Mapping(target = "deviceId", source = "device.deviceId")
    SessionValidation toSessionValidation(Session session);

    @Mappings({
            @Mapping(target = "refreshToken", source = "refreshToken", ignore = true),
            @Mapping(target = "deviceId", source = "deviceId", ignore = true)
    })
    TokenDto toTokenDto(TokenDto tokenDto);
}
