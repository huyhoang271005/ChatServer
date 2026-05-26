package social.chat.authentication.api;

import org.springframework.http.ResponseCookie;
import org.springframework.modulith.NamedInterface;
import social.chat.authentication.api.dto.SessionValidation;
import social.chat.authentication.api.dto.TokenDto;

import java.time.Duration;
import java.util.List;

@NamedInterface
public interface AuthenticationImp {
    SessionValidation createSessionByDevice(Long userId, Long deviceId, String deviceName, String deviceType,
                                            String userAgent, String ipAddress, String location);
    TokenDto generateToken(Long userId, Long deviceId);
    Long getUserIdBySessionId(Long sessionId);
    ResponseCookie getResponseCookie(String paramName, String paramValue, Duration duration);
    void updateValidatedSession(Long sessionId, boolean validated);
    void hardDeleteSessionByUserIds(List<Long> userIds);
}
