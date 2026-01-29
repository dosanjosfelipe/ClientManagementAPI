package dev.felipe.usermanagement.dto;

import java.util.List;

public record ClientResponseDTO(
        List<ClientResponseItemsDTO> clients,
        int total
) {
}
