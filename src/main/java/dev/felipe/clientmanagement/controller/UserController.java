package dev.felipe.clientmanagement.controller;

import dev.felipe.clientmanagement.dto.user.UserLoginDTO;
import dev.felipe.clientmanagement.dto.user.UserRegisterDTO;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.service.AuthService;
import dev.felipe.clientmanagement.service.UserService;
import dev.felipe.clientmanagement.utils.CookieGenerator;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import static dev.felipe.clientmanagement.security.TokenType.ACCESS;
import static dev.felipe.clientmanagement.security.TokenType.REFRESH;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@RequestBody @Valid UserLoginDTO dto, HttpServletResponse response) {

        User user = userService.authenticateUser(dto);

        String accessToken = authService.generateToken(user.getId(), user.getEmail(), user.getName(), ACCESS);
        String refreshToken = authService.generateToken(user.getId(), user.getEmail(), user.getName(), REFRESH);

        ResponseCookie accessCookie = CookieGenerator.generateCookie("access_token", accessToken, 3600);
        ResponseCookie refreshCookie = CookieGenerator.generateCookie("refresh_token", refreshToken, 604800);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("username", user.getName().split(" ")[0]));
     }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Valid UserRegisterDTO dto) {
        userService.saveUser(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message","Usuário registrado com sucesso."));
    }
}
