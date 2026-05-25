package social.chat.authentication.api;

import org.springframework.modulith.NamedInterface;
import social.chat.authentication.api.dto.TokenDto;

@NamedInterface
public interface AuthenticationImp {
    TokenDto generateToken(Long userId, Long deviceId);
    void expiredVerification();
    void hardDeleteVerification();
}
