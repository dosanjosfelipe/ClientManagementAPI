package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.model.Client;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.ClientRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.stream.Stream;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientFilesServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientFilesService clientFilesService;

    @Nested
    @DisplayName("CSV Generation Operations")
    class CSVGeneration {

        @Test
        void shouldGenerateCorrectCSVWhenClientsExist() throws IOException {
            User user = new User();
            user.setId(1L);

            Client client = new Client();
            client.setName("João Silva");
            client.setEmail("joao@email.com");
            client.setPhone("11999999999");
            client.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
            client.setUpdatedAt(LocalDateTime.of(2025, 1, 2, 0, 0));

            when(clientRepository.streamAllByOwnerId(1L)).thenReturn(Stream.of(client));
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            clientFilesService.generateCSV(user, outputStream);

            String csv = outputStream.toString(StandardCharsets.UTF_8);

            assertNotNull(csv);
            assertTrue(csv.contains("Nome"));
            assertTrue(csv.contains("Email"));
            assertTrue(csv.contains("João Silva"));
            assertTrue(csv.contains("joao@email.com"));
            assertTrue(csv.contains("2025-01-01"));
            verify(clientRepository).streamAllByOwnerId(1L);
        }

        @Test
        void shouldGenerateOnlyHeaderWhenNoClientsFound() throws IOException {
            User user = new User();
            user.setId(1L);

            when(clientRepository.streamAllByOwnerId(1L)).thenReturn(Stream.empty());
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            clientFilesService.generateCSV(user, outputStream);

            String csv = outputStream.toString(StandardCharsets.UTF_8);

            assertTrue(csv.contains("Nome,Email,Telefone"));
            assertFalse(csv.contains("@email.com"));
            verify(clientRepository).streamAllByOwnerId(1L);
        }
    }

    @Nested
    @DisplayName("CSV Import Operations")
    class CSVImport {

        @Test
        void shouldImportAndSaveClientsSuccessfully() throws IOException {
            User user = new User();
            user.setId(1L);
            String csvContent = """
                    Nome,Email,Telefone,Criado em,Atualizado em
                    Maria Silva,MARIA@EMAIL.COM,11888888888,2025-01-01,2025-01-02
                    """;

            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getInputStream()).thenReturn(
                    new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

            clientFilesService.importCSV(file, user);

            verify(clientRepository).saveAll(argThat(clients -> {
                Client saved = clients.iterator().next();
                return saved.getName().equals("Maria Silva") &&
                        saved.getEmail().equals("maria@email.com") &&
                        saved.getOwner().getId().equals(1L);
            }));
            verify(clientRepository).flush();
        }

        @Test
        void shouldThrowExceptionWhenFileIsEmpty() {
            User user = new User();
            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(true);

            assertThrows(IllegalArgumentException.class, () -> clientFilesService.importCSV(file, user));
            verify(clientRepository, never()).saveAll(any());
        }

        @Test
        void shouldThrowConstraintViolationWhenDuplicateDataFoundInDatabase() throws IOException {
            User user = new User();
            user.setId(1L);
            String csvContent = "Nome,Email,Telefone,Criado em,Atualizado em\n" +
                    "Maria,m@m.com,11,2025-01-01,2025-01-01";

            MultipartFile file = mock(MultipartFile.class);
            when(file.isEmpty()).thenReturn(false);
            when(file.getInputStream()).thenReturn(
                    new ByteArrayInputStream(csvContent.getBytes(StandardCharsets.UTF_8)));

            ConstraintViolationException exception =
                    new ConstraintViolationException("duplicate", new SQLException(), "email_unique");
            doThrow(exception).when(clientRepository).flush();

            ConstraintViolationException thrown = assertThrows(ConstraintViolationException.class,
                    () -> clientFilesService.importCSV(file, user));

            assertTrue(thrown.getMessage().contains("Campos únicos duplicados"));
            verify(clientRepository).saveAll(any());
            verify(clientRepository).flush();
        }
    }
}