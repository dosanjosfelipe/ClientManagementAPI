package dev.felipe.clientmanagement.dto.client;

import java.util.List;

public record ClientResponseDTO(
        List<ClientResponseItemsDTO> clients,
        long total
) {
}
