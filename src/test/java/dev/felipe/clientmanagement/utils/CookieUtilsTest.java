package dev.felipe.clientmanagement.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;
import static org.junit.jupiter.api.Assertions.*;

class CookieUtilsTest {

    @Nested
    @DisplayName("Cookie Generation Operations")
    class CookieGeneration {

        @Test
        void shouldGenerateCookieWithCorrectAttributesForAccessToken() {
            String name = "access_token";
            String value = "token123";
            long maxAge = 3600;

            ResponseCookie cookie = CookieUtils.generateCookie(name, value, maxAge);

            assertNotNull(cookie);
            assertEquals(name, cookie.getName());
            assertEquals(value, cookie.getValue());
            assertEquals(maxAge, cookie.getMaxAge().getSeconds());

            assertTrue(cookie.isHttpOnly());
            assertTrue(cookie.isSecure());
            assertEquals("/", cookie.getPath());
            assertEquals("Lax", cookie.getSameSite());
        }

        @Test
        void shouldGenerateCookieWithCorrectAttributesForRefreshToken() {
            String name = "refresh_token";
            String value = "refresh456";
            long maxAge = 604800;

            ResponseCookie cookie = CookieUtils.generateCookie(name, value, maxAge);

            assertEquals("refresh_token", cookie.getName());
            assertEquals("refresh456", cookie.getValue());
            assertEquals(604800, cookie.getMaxAge().getSeconds());
        }

        @Test
        void shouldEnforceSecurityDefaultsRegardlessOfInput() {
            ResponseCookie cookie = CookieUtils.generateCookie("test", "value", 1000);

            assertAll("Security Policies",
                    () -> assertTrue(cookie.isSecure(), "O cookie deve ser sempre Secure"),
                    () -> assertTrue(cookie.isHttpOnly(), "O cookie deve ser sempre HttpOnly"),
                    () -> assertEquals("/", cookie.getPath(), "O path padrão deve ser root"),
                    () -> assertEquals("Lax", cookie.getSameSite(), "O SameSite deve ser Lax por padrão")
            );
        }
    }
}