package dev.felipe.usermanagement.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserLoginDTO(
        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min=6, max=14)
        String password) {
}
