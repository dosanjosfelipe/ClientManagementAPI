package dev.felipe.usermanagement.dto;

import java.time.LocalDate;

public record ClientResponseItemsDTO (
        Long id,
        String name,
        String email,
        String phone,
        LocalDate createdAt,
        LocalDate updatedAt
){
}
