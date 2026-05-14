package com.jiaoshoujia.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.UUID;

@Component
public class JwtTokenProvider {

    private static final long ACCESS_TOKEN_MINUTES = 30;
    private static final long REFRESH_TOKEN_DAYS = 7;

    private final RsaKeyProperties rsaKeyProperties;

    public JwtTokenProvider(RsaKeyProperties rsaKeyProperties) {
        this.rsaKeyProperties = rsaKeyProperties;
    }

    public String createAccessToken(LoginUser loginUser) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(loginUser.getUsername())
                .claim("userId", loginUser.getUserId())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ACCESS_TOKEN_MINUTES, ChronoUnit.MINUTES)))
                .signWith(rsaKeyProperties.privateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public String createRefreshToken(LoginUser loginUser) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(loginUser.getUsername())
                .claim("userId", loginUser.getUserId())
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(REFRESH_TOKEN_DAYS, ChronoUnit.DAYS)))
                .signWith(rsaKeyProperties.privateKey(), Jwts.SIG.RS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(rsaKeyProperties.publicKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String getTokenId(String token) {
        return parseToken(token).getId();
    }

    public Long getUserId(String token) {
        return parseToken(token).get("userId", Long.class);
    }

    public String getUsername(String token) {
        return parseToken(token).getSubject();
    }

    public boolean isTokenExpired(String token) {
        return parseToken(token).getExpiration().before(new Date());
    }
}
