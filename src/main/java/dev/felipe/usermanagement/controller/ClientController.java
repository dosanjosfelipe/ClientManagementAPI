package dev.felipe.usermanagement.controller;

import dev.felipe.usermanagement.dto.ClientDTO;
import dev.felipe.usermanagement.dto.ClientResponseDTO;
import dev.felipe.usermanagement.dto.ClientResponseItemsDTO;
import dev.felipe.usermanagement.model.Client;
import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.service.ClientService;
import dev.felipe.usermanagement.utils.TokenUserExtractor;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/auth/clients")
public class ClientController {

    private final ClientService clientService;
    private final TokenUserExtractor tokenUserExtractor;

    public ClientController(ClientService clientService, TokenUserExtractor tokenUserExtractor) {
        this.clientService = clientService;
        this.tokenUserExtractor = tokenUserExtractor;
    }

    @PostMapping("/new")
    public ResponseEntity<Map<String, String>> newClient(@CookieValue(name = "access_token", required = false) String token,
                                    @RequestBody @Valid ClientDTO dto) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = tokenUserExtractor.extractUser(token);

        clientService.saveClient(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message","Cliente registrado com sucesso."));
    }

    @GetMapping("/show")
    public ResponseEntity<ClientResponseDTO> showClients(@CookieValue(name = "access_token", required = false) String token) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = tokenUserExtractor.extractUser(token);

        List<Client> clients = clientService.getClients(user);

        List<ClientResponseItemsDTO> response = clients.stream()
                .map(client -> new ClientResponseItemsDTO(
                        client.getId(),
                        client.getName(),
                        client.getEmail(),
                        client.getPhone(),
                        client.getCreatedAt(),
                        client.getUpdatedAt()
                ))
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(new ClientResponseDTO(response, response.size()));

    }
}
