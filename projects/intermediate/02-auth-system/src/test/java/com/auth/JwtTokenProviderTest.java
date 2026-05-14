package com.auth;

import com.auth.config.JwtTokenProvider;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private final String accessSecret = "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGZvciBhY2Nlc3MgdG9rZW4gY29udGFpbmluZyAzMiBieXRlcw==";
    private final String refreshSecret = "dGhpcyBpcyBhIHRlc3Qgc2VjcmV0IGZvciByZWZyZXNoIHRva2VuIGNvbnRhaW5pbmcgMzIgYnl0ZXM=";

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(accessSecret, refreshSecret, 3600000L, 86400000L);
    }

    @Nested
    class TokenGeneration {

        @Test
        void testGenerateAccessToken() {
            String token = tokenProvider.generateAccessToken(1L, "user@test.com", "USER");

            assertNotNull(token);
            assertFalse(token.isBlank());
            assertTrue(token.split("\\.").length == 3);
        }

        @Test
        void testGenerateRefreshToken() {
            String token = tokenProvider.generateRefreshToken(1L);

            assertNotNull(token);
            assertFalse(token.isBlank());
            assertTrue(token.split("\\.").length == 3);
        }

        @Test
        void testGenerateAccessToken_DifferentUsers() {
            String token1 = tokenProvider.generateAccessToken(1L, "a@t.com", "USER");
            String token2 = tokenProvider.generateAccessToken(2L, "b@t.com", "ADMIN");

            assertNotEquals(token1, token2);
        }

        @Test
        void testGenerateAccessToken_AdminRole() {
            String token = tokenProvider.generateAccessToken(1L, "admin@test.com", "ADMIN");

            assertNotNull(token);
        }
    }

    @Nested
    class TokenValidation {

        @Test
        void testValidateAccessToken_Valid() {
            String token = tokenProvider.generateAccessToken(1L, "user@test.com", "USER");

            assertTrue(tokenProvider.validateAccessToken(token));
        }

        @Test
        void testValidateAccessToken_Invalid() {
            assertFalse(tokenProvider.validateAccessToken("invalid-token"));
        }

        @Test
        void testValidateAccessToken_Empty() {
            assertFalse(tokenProvider.validateAccessToken(""));
        }

        @Test
        void testValidateAccessToken_Null() {
            assertFalse(tokenProvider.validateAccessToken(null));
        }

        @Test
        void testValidateRefreshToken_Valid() {
            String token = tokenProvider.generateRefreshToken(1L);

            assertTrue(tokenProvider.validateRefreshToken(token));
        }

        @Test
        void testValidateRefreshToken_Invalid() {
            assertFalse(tokenProvider.validateRefreshToken("bad-token"));
        }

        @Test
        void testValidateRefreshToken_Empty() {
            assertFalse(tokenProvider.validateRefreshToken(""));
        }
    }

    @Nested
    class ClaimsExtraction {

        @Test
        void testGetUserIdFromAccessToken() {
            String token = tokenProvider.generateAccessToken(42L, "user@test.com", "USER");

            Long userId = tokenProvider.getUserIdFromAccessToken(token);

            assertEquals(42L, userId);
        }

        @Test
        void testGetUserIdFromRefreshToken() {
            String token = tokenProvider.generateRefreshToken(99L);

            Long userId = tokenProvider.getUserIdFromRefreshToken(token);

            assertEquals(99L, userId);
        }

        @Test
        void testGetUserIdFromAccessToken_AnotherUser() {
            String token = tokenProvider.generateAccessToken(7L, "seven@test.com", "MODERATOR");

            Long userId = tokenProvider.getUserIdFromAccessToken(token);

            assertEquals(7L, userId);
        }
    }

    @Nested
    class EdgeCases {

        @Test
        void testAccessTokenWithDifferentSecretFails() {
            String token = tokenProvider.generateAccessToken(1L, "test@test.com", "USER");

            JwtTokenProvider differentProvider = new JwtTokenProvider(
                    "b3RoZXIgc2VjcmV0IGZvciB0ZXN0aW5nIHB1cnBvc2VzIG9ubHkgMzIgYnl0ZXMgbG9uZw==",
                    refreshSecret, 3600000L, 86400000L);

            assertFalse(differentProvider.validateAccessToken(token));
        }

        @Test
        void testLargeUserId() {
            String token = tokenProvider.generateAccessToken(999999999L, "big@id.com", "USER");

            assertTrue(tokenProvider.validateAccessToken(token));
            assertEquals(999999999L, tokenProvider.getUserIdFromAccessToken(token));
        }

        @Test
        void testSpecialCharsInEmail() {
            String token = tokenProvider.generateAccessToken(1L, "test+alias@test.com", "USER");

            assertTrue(tokenProvider.validateAccessToken(token));
        }
    }
}
