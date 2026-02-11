package dev.felipe.clientmanagement.security.filter;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.service.AuthService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;
import dev.felipe.clientmanagement.utils.TokenUtils;
import dev.felipe.clientmanagement.security.TokenType;

@Component
public class AccessReadTokenFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public AccessReadTokenFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String token = resolveToken(request);
        System.out.println("Token resolvido: " + token);

        if (token != null) {
            try {
                Claims claims = authService.validateToken(token);
                System.out.println("Token validado com sucesso");
                System.out.println("Tipo: " + claims.get("type"));

                if (isSupportedTokenType(claims)) {
                    User user = authService.findUserByClaim(claims);
                    System.out.println("Usuário encontrado pelo claim: " + user);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    user,
                                    null,
                                    List.of());

                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }

            } catch (Exception ignored) {
                System.out.println("Falha na validação");
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String visitorToken = TokenUtils.extractVisitorToken(request);
        if (visitorToken != null) return visitorToken;

        return TokenUtils.extractAccessToken(request);
    }

    private boolean isSupportedTokenType(Claims claims) {
        String type = claims.get("type", String.class);
        return TokenType.ACCESS.name().equals(type) || TokenType.READ.name().equals(type);
    }
}



