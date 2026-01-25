package dev.felipe.usermanagement.service;

import dev.felipe.usermanagement.dto.UserRegisterDTO;
import dev.felipe.usermanagement.exception.EmailAlreadyExistsException;
import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.repository.UserRepository;
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

        if (userRepository.existsByEmail(dto.email().toLowerCase())) {
            throw new EmailAlreadyExistsException();
        }

        User user = new User();

        user.setName(dto.name());
        user.setEmail(dto.email().toLowerCase());
        user.setPassword(passwordEncoder.encode(dto.password()));

        userRepository.save(user);
    }
}
