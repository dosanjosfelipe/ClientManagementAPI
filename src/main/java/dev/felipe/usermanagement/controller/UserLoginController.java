package dev.felipe.usermanagement.controller;

import dev.felipe.usermanagement.dto.UserLoginDTO;
import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.service.JwtService;
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
import static dev.felipe.usermanagement.security.TokenType.ACCESS;
import static dev.felipe.usermanagement.security.TokenType.REFRESH;

@RestController
@RequestMapping("/api/v1/users")
public class UserLoginController {

    private final UserService userService;
    private final JwtService jwtService;

    public UserLoginController(UserService userService, JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody @Valid UserLoginDTO dto, HttpServletResponse response) {

        User user = userService.authenticateUser(dto);

        String accessToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getName(), ACCESS);
        String refreshToken = jwtService.generateToken(user.getId(), user.getEmail(), user.getName(), REFRESH);

        ResponseCookie accessCookie = CookieGenerator.generateCookie("access_token", accessToken, 3600);
        ResponseCookie refreshCookie = CookieGenerator.generateCookie("refresh_token", refreshToken, 604800);

        response.addHeader(HttpHeaders.SET_COOKIE, accessCookie.toString());
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());

        return ResponseEntity.status(HttpStatus.OK).build();
     }
}
