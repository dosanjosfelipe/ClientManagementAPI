package dev.felipe.clientmanagement.security.filter;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.security.TokenType;
import dev.felipe.clientmanagement.service.AuthService;
import dev.felipe.clientmanagement.utils.TokenUtils;
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

@Component
public class RefreshTokenFilter extends OncePerRequestFilter {

    private final AuthService authService;

    public RefreshTokenFilter(AuthService authService) {
        this.authService = authService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String refreshToken = TokenUtils.extractRefreshToken(request);

        if (refreshToken != null) {
            try {
                Claims claims = authService.validateToken(refreshToken);
                String type = claims.get("type", String.class);

                if (TokenType.REFRESH.name().equals(type)) {
                    User user = authService.findUserByClaim(claims);

                    if (user != null) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        user,
                                        null,
                                        List.of()
                                );

                        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                }
            } catch (Exception e) {
                SecurityContextHolder.clearContext();
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return !(uri.equals("/api/v1/auth/refresh") || uri.equals("/api/v1/auth/logout"));
    }
}