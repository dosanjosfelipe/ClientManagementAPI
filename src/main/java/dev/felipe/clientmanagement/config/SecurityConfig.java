package dev.felipe.clientmanagement.config;

import dev.felipe.clientmanagement.security.CustomAuthenticationEntryPoint;
import dev.felipe.clientmanagement.security.filter.AccessReadTokenFilter;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AccessReadTokenFilter accessReadTokenFilter;

    public SecurityConfig(AccessReadTokenFilter accessReadTokenFilter) {
        this.accessReadTokenFilter = accessReadTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        http.cors(cors -> {})
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers
                                (DispatcherType.FORWARD, DispatcherType.ASYNC).permitAll()
                        .requestMatchers("/api/v1/auth/**").authenticated().anyRequest().permitAll()
                ).exceptionHandling(exception -> exception
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint()))
                .addFilterBefore(accessReadTokenFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @PostConstruct
    public void setupSecurityContext() {
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
