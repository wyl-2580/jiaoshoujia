package com.jiaoshoujia.framework.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@DisplayName("JWT Token Provider 测试")
class JwtTokenProviderTest {

    private static JwtTokenProvider tokenProvider;
    private static RSAPublicKey publicKey;

    @BeforeAll
    static void setUp() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);
        KeyPair keyPair = generator.generateKeyPair();

        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        publicKey = (RSAPublicKey) keyPair.getPublic();

        RsaKeyProperties rsaKeyProperties = mock(RsaKeyProperties.class);
        when(rsaKeyProperties.privateKey()).thenReturn(privateKey);
        when(rsaKeyProperties.publicKey()).thenReturn(publicKey);

        tokenProvider = new JwtTokenProvider(rsaKeyProperties);
    }

    private LoginUser createLoginUser() {
        return new LoginUser(1L, "admin", "password", 100L, Set.of("system:user:list"), 1);
    }

    @Test
    @DisplayName("创建 AccessToken 并正确解析")
    void createAndParseAccessToken() {
        LoginUser loginUser = createLoginUser();

        String token = tokenProvider.createAccessToken(loginUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = tokenProvider.parseToken(token);
        assertEquals("admin", claims.getSubject());
        assertEquals(1L, claims.get("userId", Long.class));
        assertNotNull(claims.getId());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("创建 RefreshToken 并正确解析")
    void createAndParseRefreshToken() {
        LoginUser loginUser = createLoginUser();

        String token = tokenProvider.createRefreshToken(loginUser);

        assertNotNull(token);

        String username = tokenProvider.getUsername(token);
        assertEquals("admin", username);

        Long userId = tokenProvider.getUserId(token);
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("获取 TokenId")
    void getTokenId() {
        LoginUser loginUser = createLoginUser();
        String token = tokenProvider.createAccessToken(loginUser);

        String tokenId = tokenProvider.getTokenId(token);

        assertNotNull(tokenId);
        assertFalse(tokenId.isEmpty());
    }

    @Test
    @DisplayName("Token 未过期")
    void isTokenExpired_notExpired() {
        LoginUser loginUser = createLoginUser();
        String token = tokenProvider.createAccessToken(loginUser);

        assertFalse(tokenProvider.isTokenExpired(token));
    }

    @Test
    @DisplayName("两次生成的 Token 不同（包含随机 JTI）")
    void tokensAreDifferent() {
        LoginUser loginUser = createLoginUser();

        String token1 = tokenProvider.createAccessToken(loginUser);
        String token2 = tokenProvider.createAccessToken(loginUser);

        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("篡改 Token 解析失败")
    void tamperToken_parseFails() {
        LoginUser loginUser = createLoginUser();
        String token = tokenProvider.createAccessToken(loginUser);

        String tampered = token.substring(0, token.length() - 5) + "xxxxx";

        assertThrows(Exception.class, () -> tokenProvider.parseToken(tampered));
    }
}
