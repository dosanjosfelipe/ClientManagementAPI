package dev.felipe.clientmanagement.utils;

import org.springframework.http.ResponseCookie;

public class CookieGenerator {

    public static ResponseCookie generateCookie(String name, String token, long maxAge) {
        return ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
    }
}
