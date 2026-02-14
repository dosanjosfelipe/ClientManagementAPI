package dev.felipe.clientmanagement.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Nested
    @DisplayName("Access Token Extraction")
    class AccessTokenExtraction {

        @Test
        void shouldReturnTokenWhenAccessTokenCookieExists() {
            Cookie[] cookies = {
                    new Cookie("access_token", "access123"),
                    new Cookie("other", "value")
            };
            when(request.getCookies()).thenReturn(cookies);

            String result = TokenUtils.extractAccessToken(request);

            assertEquals("access123", result);
        }

        @Test
        void shouldReturnNullWhenAccessTokenCookieIsMissing() {
            Cookie[] cookies = { new Cookie("other", "value") };
            when(request.getCookies()).thenReturn(cookies);

            String result = TokenUtils.extractAccessToken(request);

            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenCookiesAreNull() {
            when(request.getCookies()).thenReturn(null);

            String result = TokenUtils.extractAccessToken(request);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Refresh Token Extraction")
    class RefreshTokenExtraction {

        @Test
        void shouldReturnTokenWhenRefreshTokenCookieExists() {
            Cookie[] cookies = { new Cookie("refresh_token", "refresh123") };
            when(request.getCookies()).thenReturn(cookies);

            String result = TokenUtils.extractRefreshToken(request);

            assertEquals("refresh123", result);
        }

        @Test
        void shouldReturnNullWhenRefreshTokenCookieIsMissing() {
            Cookie[] cookies = { new Cookie("access_token", "access123") };
            when(request.getCookies()).thenReturn(cookies);

            String result = TokenUtils.extractRefreshToken(request);

            assertNull(result);
        }
    }

    @Nested
    @DisplayName("Visitor Token Extraction")
    class VisitorTokenExtraction {

        @Test
        void shouldReturnTokenWhenAuthorizationHeaderIsValid() {
            when(request.getHeader("Authorization")).thenReturn("Bearer visitorToken123");

            String result = TokenUtils.extractVisitorToken(request);

            assertEquals("visitorToken123", result);
        }

        @Test
        void shouldReturnNullWhenAuthorizationHeaderIsMissing() {
            when(request.getHeader("Authorization")).thenReturn(null);

            String result = TokenUtils.extractVisitorToken(request);

            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenPrefixIsInvalid() {
            when(request.getHeader("Authorization")).thenReturn("Basic abc123");

            String result = TokenUtils.extractVisitorToken(request);

            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenTokenIsBlank() {
            when(request.getHeader("Authorization")).thenReturn("Bearer ");

            String result = TokenUtils.extractVisitorToken(request);

            assertNull(result);
        }

        @Test
        void shouldReturnNullWhenTokenIsLiteralNullOrUndefined() {
            when(request.getHeader("Authorization")).thenReturn("Bearer null");
            assertNull(TokenUtils.extractVisitorToken(request));

            when(request.getHeader("Authorization")).thenReturn("Bearer undefined");
            assertNull(TokenUtils.extractVisitorToken(request));
        }
    }
}