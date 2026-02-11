package dev.felipe.clientmanagement.utils;

import org.springframework.http.ResponseCookie;

public class CookieUtils {

    public static ResponseCookie generateCookie(String name, String value, long maxAge) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
