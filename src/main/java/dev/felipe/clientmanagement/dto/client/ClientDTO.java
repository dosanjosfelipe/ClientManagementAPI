package dev.felipe.clientmanagement.dto.client;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClientDTO(
        @NotBlank
        @Size(min = 3, max = 40)
        String name,

        @NotBlank
        @Email
        String email,

        @Pattern(regexp = "^[0-9]+$")
        @NotBlank
        @Size(min = 10, max = 11)
        String phone

) {
}
