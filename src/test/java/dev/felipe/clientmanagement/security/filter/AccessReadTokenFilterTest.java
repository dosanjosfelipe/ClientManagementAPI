package dev.felipe.clientmanagement.security.filter;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.security.TokenType;
import dev.felipe.clientmanagement.service.AuthService;
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
import java.util.Objects;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccessReadTokenFilterTest {

    @Mock
    private AuthService authService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private Claims claims;

    private AccessReadTokenFilter filter;

    @BeforeEach
    void setUp() {
        filter = new AccessReadTokenFilter(authService);
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Successful Authentication Scenarios")
    class SuccessfulAuthentication {

        @Test
        void shouldAuthenticateWhenAccessTokenIsValid() throws Exception {
            String token = "valid-access-token";
            User user = new User();
            user.setId(1L);

            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractVisitorToken(request)).thenReturn(null);
                mockedTokenUtils.when(() -> TokenUtils.extractAccessToken(request)).thenReturn(token);

                when(authService.validateToken(token)).thenReturn(claims);
                when(claims.get("type", String.class)).thenReturn(TokenType.ACCESS.name());
                when(authService.findUserByClaim(claims)).thenReturn(user);

                filter.doFilterInternal(request, response, filterChain);

                var auth = SecurityContextHolder.getContext().getAuthentication();
                assertNotNull(auth);
                assertEquals(user, auth.getPrincipal());
                verify(filterChain).doFilter(request, response);
            }
        }

        @Test
        void shouldAuthenticateWhenReadTokenIsValid() throws Exception {
            String token = "valid-read-token";
            User user = new User();
            user.setId(2L);

            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractVisitorToken(request)).thenReturn(null);
                mockedTokenUtils.when(() -> TokenUtils.extractAccessToken(request)).thenReturn(token);

                when(authService.validateToken(token)).thenReturn(claims);
                when(claims.get("type", String.class)).thenReturn(TokenType.READ.name());
                when(authService.findUserByClaim(claims)).thenReturn(user);

                filter.doFilterInternal(request, response, filterChain);

                assertNotNull(SecurityContextHolder.getContext().getAuthentication());
                verify(filterChain).doFilter(request, response);
            }
        }
    }

    @Nested
    @DisplayName("Priority and Extraction Rules")
    class PriorityScenarios {

        @Test
        void shouldPrioritizeVisitorTokenOverAccessToken() throws Exception {
            String visitorToken = "visitor-token";
            String accessToken = "access-token";
            User visitorUser = new User();
            visitorUser.setId(99L);

            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractVisitorToken(request)).thenReturn(visitorToken);
                mockedTokenUtils.when(() -> TokenUtils.extractAccessToken(request)).thenReturn(accessToken);

                when(authService.validateToken(visitorToken)).thenReturn(claims);
                when(claims.get("type", String.class)).thenReturn(TokenType.READ.name());
                when(authService.findUserByClaim(claims)).thenReturn(visitorUser);

                filter.doFilterInternal(request, response, filterChain);

                assertEquals(visitorUser, Objects.requireNonNull(
                        SecurityContextHolder.getContext().getAuthentication()).getPrincipal());
                verify(authService).validateToken(visitorToken);
                verify(authService, never()).validateToken(accessToken);
            }
        }
    }

    @Nested
    @DisplayName("Failure and Edge Case Scenarios")
    class FailureScenarios {

        @Test
        void shouldClearContextWhenTokenIsInvalid() throws Exception {
            String token = "invalid-token";

            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractVisitorToken(request)).thenReturn(null);
                mockedTokenUtils.when(() -> TokenUtils.extractAccessToken(request)).thenReturn(token);

                when(authService.validateToken(token)).thenThrow(new RuntimeException("Expired or Malformed"));

                filter.doFilterInternal(request, response, filterChain);

                assertNull(SecurityContextHolder.getContext().getAuthentication());
                verify(filterChain).doFilter(request, response);
            }
        }

        @Test
        void shouldNotAuthenticateWhenTokenTypeIsUnsupported() throws Exception {
            String token = "refresh-token";

            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractVisitorToken(request)).thenReturn(null);
                mockedTokenUtils.when(() -> TokenUtils.extractAccessToken(request)).thenReturn(token);

                when(authService.validateToken(token)).thenReturn(claims);
                when(claims.get("type", String.class)).thenReturn("REFRESH");

                filter.doFilterInternal(request, response, filterChain);

                assertNull(SecurityContextHolder.getContext().getAuthentication());
                verify(authService, never()).findUserByClaim(any());
            }
        }

        @Test
        void shouldSkipAuthenticationWhenNoTokensArePresent() throws Exception {
            try (MockedStatic<TokenUtils> mockedTokenUtils = mockStatic(TokenUtils.class)) {
                mockedTokenUtils.when(() -> TokenUtils.extractVisitorToken(request)).thenReturn(null);
                mockedTokenUtils.when(() -> TokenUtils.extractAccessToken(request)).thenReturn(null);

                filter.doFilterInternal(request, response, filterChain);

                assertNull(SecurityContextHolder.getContext().getAuthentication());
                verify(authService, never()).validateToken(any());
                verify(filterChain).doFilter(request, response);
            }
        }
    }
}