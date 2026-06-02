package social.chat.authentication.api;

import org.springframework.http.ResponseCookie;
import org.springframework.modulith.NamedInterface;
import social.chat.authentication.api.dto.SessionValidation;
import social.chat.authentication.api.dto.TokenDto;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@NamedInterface
public interface AuthenticationImp {
    SessionValidation createSessionByDevice(Long userId, Long deviceId, String deviceName, String deviceType,
                                            String userAgent, String ipAddress, String location, boolean revoked,
                                            boolean validated);
    TokenDto generateToken(Long userId, Long deviceId, Instant timeExpired);
    Long getUserIdBySessionId(Long sessionId);
    ResponseCookie getResponseCookie(String paramName, String paramValue, Duration duration);
    void updateValidatedSession(Long sessionId, boolean validated);
    void deleteSessionByUserIds(List<Long> userIds);
    void checkSession(Long sessionId);
}
