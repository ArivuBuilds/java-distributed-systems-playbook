
package com.arivubuilds.api.security.service;

import com.arivubuilds.api.security.model.TokenRequest;
import com.arivubuilds.api.security.model.TokenResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class JwtTokenService {

    private static final String BLACKLIST_PREFIX = "revoked:jti:";

    private final JwtEncoder jwtEncoder;
    private final StringRedisTemplate redisTemplate;

    public JwtTokenService(JwtEncoder jwtEncoder, StringRedisTemplate redisTemplate) {
        this.jwtEncoder = jwtEncoder;
        this.redisTemplate = redisTemplate;
    }

    public TokenResponse mintToken(TokenRequest request) {
        Instant now = Instant.now();
        long effectiveTtl = (request.ttlSeconds() > 0) ? request.ttlSeconds() : 3600L;
        Instant expiresAt = now.plusSeconds(effectiveTtl);
        String jti = UUID.randomUUID().toString();

        List<String> scopes = (request.scopes() != null && !request.scopes().isEmpty())
                ? request.scopes()
                : List.of("vault:read");

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("https://auth.arivubuilds.com")
                .subject(request.subject())
                .id(jti)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .claim("scope", String.join(" ", scopes))
                .build();

        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();
        String tokenValue = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        return new TokenResponse(tokenValue, "Bearer", jti, expiresAt);
    }

    public void revokeToken(Jwt jwt) {
        String jti = jwt.getId();
        Instant expiresAt = jwt.getExpiresAt();
        if (jti == null || expiresAt == null) {
            return;
        }

        Duration remainingTtl = Duration.between(Instant.now(), expiresAt);
        if (!remainingTtl.isNegative() && !remainingTtl.isZero()) {
            redisTemplate.opsForValue().set(BLACKLIST_PREFIX + jti, "REVOKED", remainingTtl);
        }
    }

    public boolean isRevoked(String jti) {
        if (jti == null || jti.isBlank()) {
            return false;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + jti));
    }
}