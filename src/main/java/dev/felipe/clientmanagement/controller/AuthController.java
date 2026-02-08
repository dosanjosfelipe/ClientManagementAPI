package dev.felipe.clientmanagement.controller;

import dev.felipe.clientmanagement.dto.user.UserLoginDTO;
import dev.felipe.clientmanagement.dto.user.UserRegisterDTO;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.service.AuthService;
import dev.felipe.clientmanagement.service.UserService;
import dev.felipe.clientmanagement.utils.CookieGenerator;
import dev.felipe.clientmanagement.utils.TokenUserExtractor;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import static dev.felipe.clientmanagement.security.TokenType.ACCESS;
import static dev.felipe.clientmanagement.security.TokenType.REFRESH;

@RestController
@RequestMapping("/api/v1")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, TokenUserExtractor tokenUserExtractor, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/auth/refresh")
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

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid UserLoginDTO dto,
                                      HttpServletResponse response) {

        User user = userService.authenticateUser(dto);

        String accessToken = authService
                .generateToken(user.getId(), user.getEmail(), user.getName(), ACCESS);
        String refreshToken = authService
                .generateToken(user.getId(), user.getEmail(), user.getName(), REFRESH);

        ResponseCookie accessCookie = CookieGenerator
                .generateCookie("access_token", accessToken, 3600);
        ResponseCookie refreshCookie = CookieGenerator
                .generateCookie("refresh_token", refreshToken, 604800);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Valid UserRegisterDTO dto) {
        userService.saveUser(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message","Usuário registrado com sucesso."));
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "access_token", required = false) String accessToken,
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            HttpServletResponse response) {

        if (accessToken == null) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseCookie expiredAccessToken = CookieGenerator
                .generateCookie("access_token", accessToken, 0);
        ResponseCookie expiredRefreshToken = CookieGenerator
                .generateCookie("refresh_token", refreshToken, 0);

        response.addHeader(HttpHeaders.SET_COOKIE, expiredAccessToken.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, expiredRefreshToken.toString());

        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
