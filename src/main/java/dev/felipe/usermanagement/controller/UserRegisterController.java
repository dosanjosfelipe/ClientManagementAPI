package dev.felipe.usermanagement.controller;

import dev.felipe.usermanagement.dto.UserRegisterDTO;
import dev.felipe.usermanagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users")
public class UserRegisterController {

    private final UserService userService;

    public UserRegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@RequestBody @Valid UserRegisterDTO dto) {
        userService.saveUser(dto);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message","Usuário registrado com sucesso."));
    }
}
