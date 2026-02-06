package dev.felipe.clientmanagement.controller;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.service.UserService;
import dev.felipe.clientmanagement.utils.TokenUserExtractor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/user")
public class UserController {

    private final TokenUserExtractor tokenUserExtractor;
    private final UserService userService;

    public UserController(TokenUserExtractor tokenUserExtractor, UserService userService) {
        this.tokenUserExtractor = tokenUserExtractor;
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<Map<String, String>> getCurrentUser(
            @CookieValue(name = "access_token", required = false) String token) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = tokenUserExtractor.extractUser(token);

        String userId = String.valueOf(user.getId());
        String userName = user.getName().split(" ")[0];

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("userName", userName, "userId", userId));
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Map<String, String>> deleteUser(
            @CookieValue(name = "access_token", required = false) String token,
            @PathVariable Long id) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Usuário deletado com sucesso."));
    }
}
