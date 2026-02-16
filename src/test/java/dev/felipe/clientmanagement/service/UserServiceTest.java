package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.dto.user.UserLoginDTO;
import dev.felipe.clientmanagement.dto.user.UserRegisterDTO;
import dev.felipe.clientmanagement.dto.user.UserUpdateDTO;
import dev.felipe.clientmanagement.exception.domain.EmailAlreadyExistsException;
import dev.felipe.clientmanagement.exception.domain.InvalidCredentialsException;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.UserRepository;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        void shouldSaveUserSuccessfullyWithEmailNormalizationAndEncodedPassword() {
            UserRegisterDTO dto = new UserRegisterDTO("Felipe",
                    "FELIPE@EMAIL.COM",
                    "mySecretPassword");

            when(userRepository.findByEmail(dto.email().toLowerCase())).thenReturn(Optional.empty());
            when(passwordEncoder.encode(dto.password())).thenReturn("encodedPassword123");

            userService.saveUser(dto);

            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();

            assertEquals("felipe@email.com", capturedUser.getEmail());
            assertEquals(dto.name(), capturedUser.getName());
            assertEquals("encodedPassword123", capturedUser.getPassword());
        }

        @Test
        void shouldThrowEmailAlreadyExistsWhenEmailIsAlreadyRegistered() {
            UserRegisterDTO dto = new UserRegisterDTO("Felipe", "exist@email.com", "password");

            when(userRepository.findByEmail(dto.email().toLowerCase())).thenReturn(Optional.of(new User()));

            assertThrows(EmailAlreadyExistsException.class, () -> userService.saveUser(dto));

            verify(userRepository, never()).save(any(User.class));
            verify(passwordEncoder, never()).encode(anyString());
        }
    }

    @Nested
    @DisplayName("Authenticate Operations")
    class AuthenticateOperations {

        @Test
        void shouldAuthenticateUserSuccessfullyWhenCredentialsAreValid() {
            UserLoginDTO dto = new UserLoginDTO("USER@EMAIL.COM", "password");
            User existingUser = new User();
            existingUser.setEmail("user@email.com");
            existingUser.setPassword("encodedPassword");

            when(userRepository.findByEmail(dto.email().toLowerCase())).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(dto.password(), existingUser.getPassword())).thenReturn(true);

            User authenticatedUser = userService.authenticateUser(dto);

            assertNotNull(authenticatedUser);
            assertEquals("user@email.com", authenticatedUser.getEmail());
        }

        @Test
        void shouldThrowUsernameNotFoundWhenEmailDoesNotExist() {
            UserLoginDTO dto = new UserLoginDTO("notfound@email.com", "password");

            when(userRepository.findByEmail(dto.email().toLowerCase())).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> userService.authenticateUser(dto));

            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        void shouldThrowInvalidCredentialsWhenPasswordDoesNotMatch() {
            UserLoginDTO dto = new UserLoginDTO("user@email.com", "wrongpassword");
            User existingUser = new User();
            existingUser.setPassword("encodedPassword");

            when(userRepository.findByEmail(dto.email().toLowerCase())).thenReturn(Optional.of(existingUser));
            when(passwordEncoder.matches(dto.password(), existingUser.getPassword())).thenReturn(false);

            assertThrows(InvalidCredentialsException.class, () -> userService.authenticateUser(dto));
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        void shouldFindUserByIdSuccessfully() {
            Long userId = 1L;
            User expectedUser = new User();
            expectedUser.setId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(expectedUser));

            User result = userService.findUserById(userId);

            assertNotNull(result);
            assertEquals(userId, result.getId());
        }

        @Test
        void shouldThrowUsernameNotFoundWhenIdDoesNotExist() {
            Long userId = 99L;

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> userService.findUserById(userId));
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {

        @Test
        void shouldUpdateUserSuccessfully() {
            Long userId = 1L;
            User existingUser = new User();
            existingUser.setId(userId);
            existingUser.setName("Old Name");

            UserUpdateDTO dto = new UserUpdateDTO("New Name");

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            userService.updateUser(userId, dto);

            verify(userRepository).save(userCaptor.capture());
            User capturedUser = userCaptor.getValue();

            assertEquals("New Name", capturedUser.getName());
            assertEquals(userId, capturedUser.getId());
        }

        @Test
        void shouldThrowUsernameNotFoundWhenUpdatingNonExistentUser() {
            Long userId = 99L;
            UserUpdateDTO dto = new UserUpdateDTO("New Name");

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> userService.updateUser(userId, dto));

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        void shouldDeleteUserSuccessfully() {
            Long userId = 1L;
            User existingUser = new User();
            existingUser.setId(userId);

            when(userRepository.findById(userId)).thenReturn(Optional.of(existingUser));

            userService.deleteUser(userId);

            verify(userRepository).delete(existingUser);
        }

        @Test
        void shouldThrowUsernameNotFoundWhenDeletingNonExistentUser() {
            Long userId = 99L;

            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> userService.deleteUser(userId));

            verify(userRepository, never()).delete(any(User.class));
        }
    }
    @Nested
    @DisplayName("Find User By Claim Operations")
    class FindUserByClaimOperations {

        @Test
        void shouldFindUserSuccessfully() {
            Claims claimsMock = mock(Claims.class);
            when(claimsMock.getSubject()).thenReturn("1");

            User expectedUser = new User();
            expectedUser.setId(1L);

            when(userRepository.findById(1L)).thenReturn(Optional.of(expectedUser));

            User result = userService.findUserByClaim(claimsMock);

            assertNotNull(result);
            assertEquals(1L, result.getId());
            verify(userRepository).findById(1L);
        }

        @Test
        void shouldThrowUsernameNotFoundWhenUserDoesNotExist() {
            Claims claimsMock = mock(Claims.class);
            when(claimsMock.getSubject()).thenReturn("99");

            when(userRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(UsernameNotFoundException.class, () -> userService.findUserByClaim(claimsMock));

            verify(userRepository).findById(99L);
        }
    }
}