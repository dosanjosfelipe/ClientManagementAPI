package dev.felipe.usermanagement.service;

import dev.felipe.usermanagement.security.TokenType;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.sql.Date;
import java.time.Instant;

@Service
public class AuthService {

    private final SecretKey SECRET_KEY;

    public AuthService(@Value("${jwt.secret}") String SECRET_KEY) {

        this.SECRET_KEY = Keys.hmacShaKeyFor
                (Decoders.BASE64.decode(SECRET_KEY));
    }

    public String generateToken(Long userId, String email, String name, TokenType type) {

        Instant now = Instant.now();

        if (type == TokenType.REFRESH) {
            return Jwts.builder()
                    .setSubject(String.valueOf(userId))
                    .claim("type", TokenType.REFRESH.name())
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(now.plusSeconds(604800)))
                    .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                    .compact();
        }

        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .claim("email", email)
                .claim("name", name)
                .claim("type", TokenType.ACCESS.name())
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(20)))
                .signWith(SECRET_KEY, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims validateToken(String token) {

        try {
            return Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtException("Token inválido.");
        }
    }
}
