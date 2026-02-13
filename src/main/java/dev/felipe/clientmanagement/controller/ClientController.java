package dev.felipe.clientmanagement.controller;

import dev.felipe.clientmanagement.dto.client.ClientDTO;
import dev.felipe.clientmanagement.dto.client.ClientResponseDTO;
import dev.felipe.clientmanagement.dto.client.ClientResponseItemsDTO;
import dev.felipe.clientmanagement.model.Client;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.security.TokenType;
import dev.felipe.clientmanagement.service.AuthService;
import dev.felipe.clientmanagement.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth/clients")
public class ClientController {

    private final ClientService clientService;
    private final AuthService authService;

    public ClientController(ClientService clientService, AuthService authService) {
        this.clientService = clientService;
        this.authService = authService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<Map<String, String>> create(
            @AuthenticationPrincipal User user,
            @RequestBody @Valid ClientDTO dto) {

        clientService.saveClient(dto, user);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message","Cliente registrado com sucesso."));
    }

    // READ
    @GetMapping()
    public ResponseEntity<ClientResponseDTO> getAllClientsByUser(
            @AuthenticationPrincipal User user,
            @RequestParam int page,
            @RequestParam(required = false) String search) {

        System.out.println("Entrou no Controller");
        System.out.println("Usuário na Controller: " + user);

        Page<Client> clients = clientService.getClients(user, page, search);

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

        return ResponseEntity.status(HttpStatus.OK)
                .body(new ClientResponseDTO(response, clients.getTotalElements()));

    }

    // UPDATE
    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, String>> update(
            @PathVariable Long id, @RequestBody ClientDTO dto) {

        clientService.updateClient(id, dto);

        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("message", "Cliente editado com sucesso."));
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@PathVariable Long id) {

        clientService.deleteClient(id);

        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of("message", "Cliente deletado com sucesso."));
    }

    // SHARE
    @GetMapping("/share")
    public ResponseEntity<Map<String, String>> share(
            @AuthenticationPrincipal User user) {

        String readToken = authService.generateToken(user.getId(), null, null, TokenType.READ);

        String URL = "http://localhost:5173/dashboard/visitor?token=" + readToken;

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("shareLink", URL));
    }
}
