package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import social.chat.authentication.api.JwtProperties;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {
    JwtEncoder jwtEncoder;
    JwtProperties jwtProperties;

    public String generateJwt(Long userId, Long sessionId, Boolean isRefreshToken){
        JwsHeader jwsHeader = JwsHeader.with(SignatureAlgorithm.RS256).build();

        JwtClaimsSet jwtClaimsSet = JwtClaimsSet.builder()
                .issuer("chat Huy Hoang")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(isRefreshToken ? jwtProperties.getRefreshTokenExpire() : jwtProperties.getAccessTokenExpire()))
                .subject(userId.toString())
                .claim("sessionId", sessionId)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, jwtClaimsSet)).getTokenValue();
    }
}
