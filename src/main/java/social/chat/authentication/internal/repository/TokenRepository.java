package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.entity.Token;
import social.chat.authentication.internal.enums.TokenType;

import java.util.List;
import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findBySessionAndTokenType(Session session, TokenType tokenType);
    Optional<Token> findByTokenValue(String tokenValue);
    boolean existsBySession_SessionIdAndTokenType(Long sessionId, TokenType tokenType);
    Integer deleteBySession_SessionIdAndTokenType(Long sessionId, TokenType tokenType);

    @Query("""
            select t.tokenValue
            from Session s
            join s.tokens t
            where s.userId in :userIds
            and t.tokenType = TokenType.FCM_TOKEN
            and t.tokenValue is not null
            """)
    List<String> findFcmTokeValueByUserIds(List<Long> userIds);
}