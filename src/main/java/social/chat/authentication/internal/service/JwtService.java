package social.chat.authentication.internal.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import social.chat.authentication.api.JwtProperties;
import social.chat.authentication.api.dto.JwtResponse;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JwtService {
    JwtEncoder jwtEncoder;
    JwtDecoder jwtDecoder;
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

    // Map with userId, sessionId
    public JwtResponse decoderJwt(String jwt) {
        Jwt jwt1 = jwtDecoder.decode(jwt);
        Long userId = Long.parseLong(jwt1.getSubject());
        Long sessionId = Long.parseLong(jwt1.getClaim("sessionId"));
        return JwtResponse.builder()
                .userId(userId)
                .sessionId(sessionId)
                .build();
    }
}
