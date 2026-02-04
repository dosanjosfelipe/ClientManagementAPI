package dev.felipe.clientmanagement.dto.client;

import java.time.LocalDateTime;

public record ClientResponseItemsDTO (
        Long id,
        String name,
        String email,
        String phone,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
){
}
