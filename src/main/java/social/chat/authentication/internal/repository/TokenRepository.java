package social.chat.authentication.internal.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import social.chat.authentication.internal.entity.Session;
import social.chat.authentication.internal.entity.Token;
import social.chat.authentication.internal.enums.TokenType;

import java.util.Optional;

public interface TokenRepository extends JpaRepository<Token, Long> {
    Optional<Token> findBySessionAndTokenType(Session session, TokenType tokenType);
    Optional<Token> findByTokenValue(String tokenValue);
}