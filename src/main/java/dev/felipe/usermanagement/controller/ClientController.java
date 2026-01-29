package dev.felipe.usermanagement.controller;

import dev.felipe.usermanagement.dto.ClientDTO;
import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.repository.UserRepository;
import dev.felipe.usermanagement.service.ClientService;
import dev.felipe.usermanagement.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("api/v1/auth/clients")
public class ClientController {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final ClientService clientService;

    public ClientController(JwtService jwtService, UserRepository userRepository, ClientService clientService) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.clientService = clientService;
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> newClient(@CookieValue(name = "access_token", required = false) String token,
                                    @RequestBody @Valid ClientDTO dto) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Claims claims = jwtService.validateToken(token);

        User user = userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new UsernameNotFoundException
                        ("Usuário não encontrado. Tente outro ou registre-se."));


        clientService.saveClient(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message","Usuário registrado com sucesso."));
    }
}
