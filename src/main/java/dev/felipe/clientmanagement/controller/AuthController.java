package dev.felipe.usermanagement.controller;

import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.service.AuthService;
import dev.felipe.usermanagement.utils.CookieGenerator;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static dev.felipe.usermanagement.security.TokenType.ACCESS;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/refresh")
    public ResponseEntity<Void> refreshAuth(@CookieValue(value = "refresh_token",
            required = false) String refreshToken, HttpServletResponse response) {

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Claims claims = authService.validateToken(refreshToken);
        String type = claims.get("type", String.class);

        if (!"REFRESH".equals(type)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = authService.findUserByClaim(claims);

        String newAccessToken = authService.
                generateToken(user.getId(), user.getEmail(), user.getName(), ACCESS);
        ResponseCookie newAccessCookie = CookieGenerator
                .generateCookie("access_token", newAccessToken,3600);

        response.addHeader(HttpHeaders.SET_COOKIE, newAccessCookie.toString());

        return ResponseEntity.status(HttpStatus.OK).build();

    }
}
