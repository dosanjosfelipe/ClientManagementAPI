package dev.felipe.clientmanagement.dto.user;

import jakarta.validation.constraints.NotBlank;

public record UserUpdateDTO(
        @NotBlank
        String username) {
}
