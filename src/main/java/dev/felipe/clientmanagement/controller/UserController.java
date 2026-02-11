package dev.felipe.clientmanagement.controller;

import dev.felipe.clientmanagement.dto.user.UserUpdateDTO;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping()
    public ResponseEntity<Map<String, String>> getCurrentUser(
            @AuthenticationPrincipal User user) {

        System.out.println("ENTROU NO CONTROLLER DO USER");
        System.out.println("O user é: " + user.getName());
        String userId = String.valueOf(user.getId());
        String userName = user.getName();

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("userName", userName, "userId", userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@AuthenticationPrincipal User user,
                                                          @PathVariable Long id) {

        if (!user.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.deleteUser(id);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Usuário deletado com sucesso."));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> updateUser(
            @AuthenticationPrincipal User user,
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDTO dto) {

        if (!user.getId().equals(id)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        userService.updateUser(id, dto);

        return ResponseEntity.status(HttpStatus.OK)
                .body(Map.of("message", "Usuário editado com sucesso."));
    }
}
