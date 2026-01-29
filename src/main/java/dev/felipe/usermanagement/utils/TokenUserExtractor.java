package dev.felipe.usermanagement.utils;

import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.repository.UserRepository;
import dev.felipe.usermanagement.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class TokenUserExtractor {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public TokenUserExtractor(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public User extractUser(String token) {
        Claims claims = jwtService.validateToken(token);

        return userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() -> new UsernameNotFoundException
                        ("Usuário não encontrado. Tente outro ou registre-se."));
    }
}
