package dev.felipe.clientmanagement.controller;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.service.ClientFilesService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import java.io.IOException;

@RestController
@RequestMapping("api/v1/auth/clients/files/")
public class ClientFilesController {

    private final ClientFilesService clientFilesService;;

    public ClientFilesController(ClientFilesService clientFilesService) {
        this.clientFilesService = clientFilesService;
    }

    @GetMapping("/export")
    public ResponseEntity<StreamingResponseBody> export(
            @AuthenticationPrincipal User user
    ) {

        StreamingResponseBody stream = outputStream -> {
            clientFilesService.generateCSV(user, outputStream);
        };

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"clients.csv\"")
                .contentType(MediaType.valueOf("text/csv"))
                .body(stream);
    }

    @PostMapping("/import")
    public ResponseEntity<Void> importClients(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) throws IOException {

        clientFilesService.importCSV(file, user);

        return ResponseEntity.ok().build();
    }
}
