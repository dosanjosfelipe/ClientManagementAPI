package dev.felipe.usermanagement.service;

import dev.felipe.usermanagement.dto.UserLoginDTO;
import dev.felipe.usermanagement.dto.UserRegisterDTO;
import dev.felipe.usermanagement.exception.EmailAlreadyExistsException;
import dev.felipe.usermanagement.exception.EmailNotFound;
import dev.felipe.usermanagement.exception.InvalidCredentials;
import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public void saveUser(UserRegisterDTO dto) {

        if (userRepository.findByEmail(dto.email().toLowerCase()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();

        user.setName(dto.name());
        user.setEmail(dto.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(user);
    }

    public String authenticateUser(UserLoginDTO dto) {

        User user = userRepository.findByEmail(dto.email())
                .orElseThrow(EmailNotFound::new);

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new InvalidCredentials();
        }

        return jwtService.generateToken(user.getId(), user.getEmail(), user.getName());
    }
}
