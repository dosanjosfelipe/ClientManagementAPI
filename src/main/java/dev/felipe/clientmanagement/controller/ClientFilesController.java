package dev.felipe.clientmanagement.controller;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.service.ClientFilesService;
import dev.felipe.clientmanagement.utils.TokenUserExtractor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.Objects;

@RestController
@RequestMapping("api/v1/auth/clients/files/")
public class ClientFilesController {

    private final ClientFilesService clientFilesService;
    private final TokenUserExtractor tokenUserExtractor;

    public ClientFilesController(ClientFilesService clientFilesService, TokenUserExtractor tokenUserExtractor) {
        this.clientFilesService = clientFilesService;
        this.tokenUserExtractor = tokenUserExtractor;
    }

    @GetMapping("/export/{id}")
    public ResponseEntity<StreamingResponseBody> export(
            @CookieValue(name = "access_token", required = false) String token,
            @PathVariable String id) {

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = tokenUserExtractor.extractUser(token);

        if (!Objects.equals(user.getId(), Long.valueOf(id))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        StreamingResponseBody stream = outputStream -> {
            clientFilesService.generateCSV(user.getId(), outputStream);
        };

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"clients.csv\"")
                .contentType(MediaType.valueOf("text/csv"))
                .body(stream);
    }
}
