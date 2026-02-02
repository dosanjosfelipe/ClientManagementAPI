package dev.felipe.clientmanagement.controller;

import dev.felipe.clientmanagement.dto.client.ClientDTO;
import dev.felipe.clientmanagement.dto.client.ClientResponseDTO;
import dev.felipe.clientmanagement.dto.client.ClientResponseItemsDTO;
import dev.felipe.clientmanagement.model.Client;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.service.ClientService;
import dev.felipe.clientmanagement.utils.TokenUserExtractor;
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

    @PostMapping
    public ResponseEntity<Map<String, String>> create(@CookieValue(name = "access_token", required = false) String token,
                                    @RequestBody @Valid ClientDTO dto) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = tokenUserExtractor.extractUser(token);

        clientService.saveClient(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message","Cliente registrado com sucesso."));
    }

    @GetMapping()
    public ResponseEntity<ClientResponseDTO> getAllClientsByUser(
            @RequestParam int page,
            @CookieValue(name = "access_token", required = false) String token) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = tokenUserExtractor.extractUser(token);

        List<Client> clients = clientService.getClients(user, page);

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

        int clientsSize = clientService.getClientsSize(user);

        return ResponseEntity.status(HttpStatus.OK).body(new ClientResponseDTO(response, clientsSize));

    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(@PathVariable String id,
                                       @CookieValue(name = "access_token", required = false) String token,
                                       @RequestBody ClientDTO dto) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        clientService.updateClient(Long.valueOf(id), dto);

        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("message", "Cliente editado com sucesso."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable String id,
                                                      @CookieValue(name = "access_token") String token) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        clientService.deleteClient(Long.valueOf(id));

        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("message", "Cliente deletado com sucesso."));
    }
}
