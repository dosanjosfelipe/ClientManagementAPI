package dev.felipe.clientmanagement.security.filter;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.security.TokenType;
import dev.felipe.clientmanagement.security.JwtService;
import dev.felipe.clientmanagement.service.UserService;
import dev.felipe.clientmanagement.utils.TokenUtils;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshTokenFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserService userService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private RefreshTokenFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RefreshTokenFilter(jwtService, userService);
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Filter Internal Logic")
    class FilterInternalLogic {

        @Test
        void shouldAuthenticateWhenRefreshTokenIsValid() throws Exception {
            String token = "valid-refresh-token";
            User user = new User();
            user.setId(1L);

            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractRefreshToken(request)).thenReturn(token);

                when(jwtService.validateToken(token)).thenReturn(claims);
                when(claims.get("type", String.class)).thenReturn(TokenType.REFRESH.name());
                when(userService.findUserByClaim(claims)).thenReturn(user);

                filter.doFilterInternal(request, response, filterChain);

                var auth = SecurityContextHolder.getContext().getAuthentication();
                assertNotNull(auth);
                assertEquals(user, auth.getPrincipal());
                verify(filterChain).doFilter(request, response);
            }
        }

        @Test
        void shouldNotAuthenticateWhenTokenTypeIsWrong() throws Exception {
            String token = "access-token-wrongly-used";

            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractRefreshToken(request)).thenReturn(token);

                when(jwtService.validateToken(token)).thenReturn(claims);
                when(claims.get("type", String.class)).thenReturn(TokenType.ACCESS.name());

                filter.doFilterInternal(request, response, filterChain);

                assertNull(SecurityContextHolder.getContext().getAuthentication());
                verify(userService, never()).findUserByClaim(any());
                verify(filterChain).doFilter(request, response);
            }
        }

        @Test
        void shouldClearContextWhenValidationFails() throws Exception {
            String token = "invalid-token";

            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractRefreshToken(request)).thenReturn(token);
                when(jwtService.validateToken(token)).thenThrow(new RuntimeException("Expired"));

                filter.doFilterInternal(request, response, filterChain);

                assertNull(SecurityContextHolder.getContext().getAuthentication());
                verify(filterChain).doFilter(request, response);
            }
        }

        @Test
        void shouldContinueChainWhenNoTokenIsPresent() throws Exception {
            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractRefreshToken(request)).thenReturn(null);

                filter.doFilterInternal(request, response, filterChain);

                assertNull(SecurityContextHolder.getContext().getAuthentication());
                verify(filterChain).doFilter(request, response);
            }
        }
    }

    @Nested
    @DisplayName("Filter Strategy (shouldNotFilter)")
    class FilterStrategy {

        @Test
        void shouldReturnFalseForRefreshEndpoint() {
            when(request.getRequestURI()).thenReturn("/api/v1/auth/refresh");

            // shouldNotFilter = false significa que o filtro SERÁ executado
            assertFalse(filter.shouldNotFilter(request));
        }

        @Test
        void shouldReturnFalseForLogoutEndpoint() {
            when(request.getRequestURI()).thenReturn("/api/v1/auth/logout");

            assertFalse(filter.shouldNotFilter(request));
        }

        @Test
        void shouldReturnTrueForOtherEndpoints() {
            when(request.getRequestURI()).thenReturn("/api/v1/clients");

            // shouldNotFilter = true significa que o filtro será IGNORADO
            assertTrue(filter.shouldNotFilter(request));
        }
    }
}