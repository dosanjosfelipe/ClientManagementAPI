package dev.felipe.usermanagement.controller;

import dev.felipe.usermanagement.dto.user.UserLoginDTO;
import dev.felipe.usermanagement.dto.user.UserRegisterDTO;
import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.service.AuthService;
import dev.felipe.usermanagement.service.UserService;
import dev.felipe.usermanagement.utils.CookieGenerator;
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
import static dev.felipe.usermanagement.security.TokenType.ACCESS;
import static dev.felipe.usermanagement.security.TokenType.REFRESH;

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
    public ResponseEntity<Void> login(@RequestBody @Valid UserLoginDTO dto, HttpServletResponse response) {

        User user = userService.authenticateUser(dto);

        String accessToken = authService.generateToken(user.getId(), user.getEmail(), user.getName(), ACCESS);
        String refreshToken = authService.generateToken(user.getId(), user.getEmail(), user.getName(), REFRESH);

        ResponseCookie accessCookie = CookieGenerator.generateCookie("access_token", accessToken, 3600);
        ResponseCookie refreshCookie = CookieGenerator.generateCookie("refresh_token", refreshToken, 604800);

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
}
