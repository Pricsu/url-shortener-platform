package com.urlshortener.user_service;

import com.urlshortener.user_service.security.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp(){
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "test-secret-key-at-least-32-characters-long");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void tokenValid_throwsForExpiredToken(){
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String expiredToken = jwtUtil.generateToken("Alfred", 5L);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.isTokenExpired(expiredToken));
    }

    @Test
    void tokenValid_success(){
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1000L);
        String token = jwtUtil.generateToken("Alfred", 5L);

        assertThat(jwtUtil.isTokenExpired(token)).isEqualTo(false);
    }

    @Test
    void validToken_success(){
        ReflectionTestUtils.setField(jwtUtil, "expiration", 11100L);
        String token = jwtUtil.generateToken("Alfred", 5L);

        assertThat(jwtUtil.isValidToken(token, "Alfred")).isEqualTo(true);
    }

    @Test
    void validToken_usernameNotMatch(){
        ReflectionTestUtils.setField(jwtUtil, "expiration", 11100L);
        String token = jwtUtil.generateToken("Alfred", 5L);

        assertThat(jwtUtil.isValidToken(token, "Anett")).isEqualTo(false);
    }

    @Test
    void validToken_expiredToken(){
        ReflectionTestUtils.setField(jwtUtil, "expiration", -1000L);
        String token = jwtUtil.generateToken("Alfred", 5L);

        assertThrows(ExpiredJwtException.class, () -> jwtUtil.isValidToken(token, "Alfred"));
    }
}
