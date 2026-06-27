package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.authentication.api.dto.UserIdWithFcmToken;
import social.chat.authentication.internal.entity.Device;
import social.chat.authentication.internal.entity.Token;
import social.chat.authentication.internal.enums.TokenType;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findByDeviceAndTokenType(Device device, TokenType tokenType);
    boolean existsByDevice_DeviceIdAndTokenType(Long deviceId, TokenType tokenType);
    Integer deleteByDevice_DeviceIdAndTokenType(Long sessionId, TokenType tokenType);

    @Query("""
            select distinct s.userId, t.tokenValue
            from Session s
            join s.device d
            join d.tokens t
            where s.userId in :userIds
            and t.tokenType = TokenType.FCM_TOKEN
            and t.tokenValue is not null
            """)
    List<UserIdWithFcmToken> findFcmTokeValueByUserIds(List<Long> userIds);
}