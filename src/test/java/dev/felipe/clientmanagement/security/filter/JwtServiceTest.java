package dev.felipe.clientmanagement.security.filter;

import dev.felipe.clientmanagement.security.JwtService;
import dev.felipe.clientmanagement.security.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        // Chave aleatória criada para teste
        String VALID_SECRET_KEY = "VGhpcy1pcy1hLXZlcnktc2VjdXJlLWFuZC1sb25nLXNlY3JldC1rZXk=";
        jwtService = new JwtService(VALID_SECRET_KEY);
    }

    @Nested
    @DisplayName("Generate Token Operations")
    class GenerateTokenOperations {

        @Test
        void shouldGenerateAccessTokenSuccessfully() {
            String token = jwtService.generateToken(1L, "felipe@email.com", "Felipe", TokenType.ACCESS);

            assertNotNull(token);

            // Validamos as claims geradas lendo o próprio token
            Claims claims = jwtService.validateToken(token);
            assertEquals("1", claims.getSubject());
            assertEquals("felipe@email.com", claims.get("email"));
            assertEquals("Felipe", claims.get("name"));
            assertEquals("ACCESS", claims.get("type"));
        }

        @Test
        void shouldGenerateRefreshTokenSuccessfully() {
            String token = jwtService.generateToken(1L, "felipe@email.com", "Felipe", TokenType.REFRESH);

            assertNotNull(token);

            Claims claims = jwtService.validateToken(token);
            assertEquals("1", claims.getSubject());
            assertEquals("REFRESH", claims.get("type"));
            assertNull(claims.get("email"));
            assertNull(claims.get("name"));
        }

        @Test
        void shouldGenerateReadTokenSuccessfullyAsFallback() {
            String token = jwtService.generateToken(1L, "felipe@email.com", "Felipe", TokenType.READ);

            assertNotNull(token);

            Claims claims = jwtService.validateToken(token);
            assertEquals("1", claims.getSubject());
            assertEquals("READ", claims.get("type"));
            assertNull(claims.get("email"));
            assertNull(claims.get("name"));
        }

        @Test
        void shouldHandleNullEmailAndNameGracefullyForAccessToken() {
            String token = jwtService.generateToken(1L, null, null, TokenType.ACCESS);

            assertNotNull(token);

            Claims claims = jwtService.validateToken(token);
            assertEquals("1", claims.getSubject());
            assertEquals("unknown@email", claims.get("email"));
            assertEquals("unknown", claims.get("name"));
            assertEquals("ACCESS", claims.get("type"));
        }
    }

    @Nested
    @DisplayName("Validate Token Operations")
    class ValidateTokenOperations {

        @Test
        void shouldValidateTokenSuccessfully() {
            String token = jwtService.generateToken(1L, "felipe@email.com", "Felipe", TokenType.ACCESS);

            Claims claims = assertDoesNotThrow(() -> jwtService.validateToken(token));

            assertNotNull(claims);
            assertEquals("1", claims.getSubject());
        }

        @Test
        void shouldThrowJwtExceptionWhenTokenIsNull() {
            JwtException exception = assertThrows(JwtException.class, () -> jwtService.validateToken(null));

            assertEquals("Token Null", exception.getMessage());
        }

        @Test
        void shouldThrowJwtExceptionWhenTokenIsInvalidOrMalformed() {
            String invalidToken = "header.payload.signature_invalid";

            JwtException exception = assertThrows(JwtException.class, () -> jwtService.validateToken(invalidToken));

            assertEquals("Token inválido.", exception.getMessage());
        }
    }
}