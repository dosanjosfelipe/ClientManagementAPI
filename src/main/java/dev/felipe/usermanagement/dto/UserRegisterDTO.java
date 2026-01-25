package dev.felipe.usermanagement.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegisterDTO(
        @NotBlank
        @Size(min=3, max=40)
        String name,

        @NotBlank
        @Email
        String email,

        @NotBlank
        @Size(min=6, max=14)
        String password) {
}
