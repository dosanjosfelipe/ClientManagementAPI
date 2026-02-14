package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.UserRepository;
import dev.felipe.clientmanagement.security.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        // Chave aleatória criada para teste
        String VALID_SECRET_KEY = "VGhpcy1pcy1hLXZlcnktc2VjdXJlLWFuZC1sb25nLXNlY3JldC1rZXk=";
        authService = new AuthService(VALID_SECRET_KEY, userRepository);
    }

    @Nested
    @DisplayName("Generate Token Operations")
    class GenerateTokenOperations {

        @Test
        void shouldGenerateAccessTokenSuccessfully() {
            String token = authService.generateToken(1L, "felipe@email.com", "Felipe", TokenType.ACCESS);

            assertNotNull(token);

            // Validamos as claims geradas lendo o próprio token
            Claims claims = authService.validateToken(token);
            assertEquals("1", claims.getSubject());
            assertEquals("felipe@email.com", claims.get("email"));
            assertEquals("Felipe", claims.get("name"));
            assertEquals("ACCESS", claims.get("type"));
        }

        @Test
        void shouldGenerateRefreshTokenSuccessfully() {
            String token = authService.generateToken(1L, "felipe@email.com", "Felipe", TokenType.REFRESH);

            assertNotNull(token);

            Claims claims = authService.validateToken(token);
            assertEquals("1", claims.getSubject());
            assertEquals("REFRESH", claims.get("type"));
            assertNull(claims.get("email"));
            assertNull(claims.get("name"));
        }

        @Test
        void shouldGenerateReadTokenSuccessfullyAsFallback() {
            String token = authService.generateToken(1L, "felipe@email.com", "Felipe", TokenType.READ);

            assertNotNull(token);

            Claims claims = authService.validateToken(token);
            assertEquals("1", claims.getSubject());
            assertEquals("READ", claims.get("type"));
            assertNull(claims.get("email"));
            assertNull(claims.get("name"));
        }

        @Test
        void shouldHandleNullEmailAndNameGracefullyForAccessToken() {
            String token = authService.generateToken(1L, null, null, TokenType.ACCESS);

            assertNotNull(token);

            Claims claims = authService.validateToken(token);
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
            String token = authService.generateToken(1L, "felipe@email.com", "Felipe", TokenType.ACCESS);

            Claims claims = assertDoesNotThrow(() -> authService.validateToken(token));

            assertNotNull(claims);
            assertEquals("1", claims.getSubject());
        }

        @Test
        void shouldThrowJwtExceptionWhenTokenIsNull() {
            JwtException exception = assertThrows(JwtException.class, () -> authService.validateToken(null));

            assertEquals("Token Null", exception.getMessage());
        }

        @Test
        void shouldThrowJwtExceptionWhenTokenIsInvalidOrMalformed() {
            String invalidToken = "header.payload.signature_invalid";

            JwtException exception = assertThrows(JwtException.class, () -> authService.validateToken(invalidToken));

            assertEquals("Token inválido.", exception.getMessage());
        }
    }

    @Nested
    @DisplayName("Find User By Claim Operations")
    class FindUserByClaimOperations {

        @Test
        void shouldFindUserSuccessfully() {
            Claims claimsMock = mock(Claims.class);
            when(claimsMock.getSubject()).thenReturn("1");

            User expectedUser = new User();
            expectedUser.setId(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(expectedUser));

            User result = authService.findUserByClaim(claimsMock);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(userRepository).findById(1L);
        }

        @Test
        void shouldThrowUsernameNotFoundWhenUserDoesNotExist() {
            Claims claimsMock = mock(Claims.class);
            when(claimsMock.getSubject()).thenReturn("99");

            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> authService.findUserByClaim(claimsMock));

            verify(userRepository).findById(99L);
        }
    }
}