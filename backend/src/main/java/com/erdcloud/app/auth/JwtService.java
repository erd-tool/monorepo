package com.erdcloud.app.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessTokenExpirationSeconds;

    public JwtService(
        @Value("${app.jwt.secret}") String secret,
        @Value("${app.jwt.access-token-expiration-seconds}") long accessTokenExpirationSeconds
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpirationSeconds = accessTokenExpirationSeconds;
    }

    public String generateAccessToken(UserPrincipal principal) {
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(principal.loginId())
            .claim("userId", principal.id())
            .claim("displayName", principal.displayName())
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(accessTokenExpirationSeconds)))
            .signWith(key)
            .compact();
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }
}
