package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.UserRepository;
import dev.felipe.clientmanagement.security.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.sql.Date;
import java.time.Instant;

@Service
public class AuthService {

    private final SecretKey SECRET_KEY;
    private final UserRepository userRepository;

    public AuthService(@Value("${jwt.secret}") String secretKey,
                       UserRepository userRepository) {

        this.SECRET_KEY = Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(secretKey)
        );

        this.userRepository = userRepository;
    }

    public String generateToken(Long userId, String email, String name, TokenType type) {

        email = email != null ? email : "unknown@email";
        name  = name  != null ? name  : "unknown";

        Instant now = Instant.now();

        if (type == TokenType.REFRESH) {
            return Jwts.builder()
                    .subject(String.valueOf(userId))
                    .claim("type", TokenType.REFRESH.name())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusSeconds(604800)))
                    .signWith(SECRET_KEY)
                    .compact();
        }

        if (type == TokenType.ACCESS) {
            return Jwts.builder()
                    .subject(String.valueOf(userId))
                    .claim("email", email)
                    .claim("name", name)
                    .claim("type", TokenType.ACCESS.name())
                    .issuedAt(Date.from(now))
                    .expiration(Date.from(now.plusSeconds(3600)))
                    .signWith(SECRET_KEY)
                    .compact();
        }

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", TokenType.READ.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(3600)))
                .signWith(SECRET_KEY)
                .compact();

    }

    public Claims validateToken(String token) {

        if (token == null) {
            throw new JwtException("Token Null");
        }

        try {
            return Jwts.parser()
                    .verifyWith(SECRET_KEY)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Token inválido.");
        }
    }

    public User findUserByClaim(Claims claims) {
        return userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado. Tente outro ou registre-se."));
    }
}
