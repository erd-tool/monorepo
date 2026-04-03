package com.erdcloud.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID = "X-USER-ID";
    private static final String USER_LOGIN_ID = "X-USER-LOGIN-ID";
    private static final String USER_ROLE = "X-USER-ROLE";
    private static final String USER_EMAIL = "X-USER-EMAIL";
    private static final String REQUEST_ID = "X-REQUEST-ID";

    private final SecretKey key;

    public JwtGatewayFilter(@Value("${app.jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // 인증 불필요 경로는 통과
        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String token = resolveToken(exchange);
        if (token == null || token.isBlank()) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        try {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove(USER_ID);
                    headers.remove(USER_LOGIN_ID);
                    headers.remove(USER_ROLE);
                    headers.remove(USER_EMAIL);
                    headers.remove(REQUEST_ID);
                    headers.set(USER_ID, claims.get("userId", Long.class).toString());
                    headers.set(USER_LOGIN_ID, claims.getSubject());
                    setIfPresent(headers, USER_ROLE, claims.get("role", String.class));
                    setIfPresent(headers, USER_EMAIL, claims.get("email", String.class));
                    headers.set(REQUEST_ID, UUID.randomUUID().toString());
                })
                .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    private boolean isPublicPath(String path) {
        return path.equals("/api/auth/signup")
            || path.equals("/api/auth/login")
            || path.equals("/healthz");
    }

    private String resolveToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            return authHeader.substring(BEARER_PREFIX.length());
        }
        return exchange.getRequest().getQueryParams().getFirst("access_token");
    }

    private void setIfPresent(HttpHeaders headers, String headerName, String value) {
        if (value != null && !value.isBlank()) {
            headers.set(headerName, value);
        }
    }
}
