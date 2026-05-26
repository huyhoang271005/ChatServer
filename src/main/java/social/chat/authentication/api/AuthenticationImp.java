package social.chat.authentication.api;

import org.springframework.modulith.NamedInterface;
import social.chat.authentication.api.dto.TokenDto;

import java.util.List;

@NamedInterface
public interface AuthenticationImp {
    TokenDto generateToken(Long userId, Long deviceId);
    void hardDeleteSessionByUserIds(List<Long> userIds);
    void expiredVerification();
    void hardDeleteVerification();
}
