package dev.felipe.usermanagement.dto.client;

import java.util.List;

public record ClientResponseDTO(
        List<ClientResponseItemsDTO> clients,
        int total
) {
}
