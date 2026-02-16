package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.dto.user.UserLoginDTO;
import dev.felipe.clientmanagement.dto.user.UserRegisterDTO;
import dev.felipe.clientmanagement.dto.user.UserUpdateDTO;
import dev.felipe.clientmanagement.exception.domain.EmailAlreadyExistsException;
import dev.felipe.clientmanagement.exception.domain.InvalidCredentialsException;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void saveUser(UserRegisterDTO dto) {

        if (userRepository.findByEmail(dto.email().toLowerCase()).isPresent()) {
            throw new EmailAlreadyExistsException("Esse email já existe em outro cliente.");
        }

        User user = new User();

        user.setName(dto.name());
        user.setEmail(dto.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(user);
    }

    public User authenticateUser(UserLoginDTO dto) {

        User user = userRepository.findByEmail(dto.email().toLowerCase())
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado. Tente outro ou registre-se."));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Senha inválida.");
        }

        return user;
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(() ->
                new UsernameNotFoundException("Usuário não encontrado. Tente outro ou registre-se."));
    }

    public void deleteUser(Long id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    public void updateUser(Long id, UserUpdateDTO dto) {
        User user = findUserById(id);

        user.setName(dto.username());

        userRepository.save(user);
    }

    public User findUserByClaim(Claims claims) {
        return userRepository.findById(Long.valueOf(claims.getSubject()))
                .orElseThrow(() ->
                        new UsernameNotFoundException("Usuário não encontrado. Tente outro ou registre-se."));
    }
}
