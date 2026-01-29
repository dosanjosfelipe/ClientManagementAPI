package dev.felipe.usermanagement.utils;

import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.repository.UserRepository;
import dev.felipe.usermanagement.service.AuthService;
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
