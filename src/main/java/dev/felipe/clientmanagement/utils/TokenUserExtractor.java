package dev.felipe.clientmanagement.utils;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.UserRepository;
import dev.felipe.clientmanagement.service.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class TokenUserExtractor {

    private final AuthService authService;
    private final UserRepository userRepository;

    public TokenUserExtractor(AuthService authService, UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
    }

    public User extractUser(String token) {
        Claims claims = authService.validateToken(token);


        return userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new UsernameNotFoundException
                        ("Usuário não encontrado. Tente outro ou registre-se."));
    }
}
