package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.dto.client.ClientDTO;
import dev.felipe.clientmanagement.exception.domain.*;
import dev.felipe.clientmanagement.model.Client;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.ClientRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientService clientService;

    @Captor
    private ArgumentCaptor<Client> clientCaptor;

    @Captor
    private ArgumentCaptor<PageRequest> pageRequestCaptor;

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        void shouldSaveClientSuccessfullyWithEmailNormalization() {
            User user = new User();
            user.setId(1L);
            ClientDTO dto = new ClientDTO("Linus Torvalds",
                    "LINUS.TORVALDS@EMAIL.COM",
                    "1234567890");

            when(clientRepository.existsClientByEmail(dto.email())).thenReturn(false);
            when(clientRepository.existsClientByPhone(dto.phone())).thenReturn(false);

            clientService.saveClient(dto, user);

            verify(clientRepository).save(clientCaptor.capture());
            Client capturedClient = clientCaptor.getValue();

            assertEquals("linus.torvalds@email.com", capturedClient.getEmail());
            assertEquals(dto.name(), capturedClient.getName());
            assertEquals(dto.phone(), capturedClient.getPhone());
            assertEquals(user, capturedClient.getOwner());
        }

        @Test
        void shouldThrowEmailAlreadyExistsAndNotCheckPhone() {
            User user = new User();
            ClientDTO dto = new ClientDTO("Ada", "exist@email.com", "111");

            when(clientRepository.existsClientByEmail(dto.email())).thenReturn(true);

            assertThrows(EmailAlreadyExistsException.class,
                    () -> clientService.saveClient(dto, user));

            verify(clientRepository, never()).existsClientByPhone(anyString());
            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        void shouldThrowPhoneAlreadyExists() {
            User user = new User();
            ClientDTO dto = new ClientDTO("Ada", "new@email.com", "999");

            when(clientRepository.existsClientByEmail(dto.email())).thenReturn(false);
            when(clientRepository.existsClientByPhone(dto.phone())).thenReturn(true);

            assertThrows(PhoneAlreadyExistsException.class,
                    () -> clientService.saveClient(dto, user));

            verify(clientRepository, never()).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("Get Operations")
    class GetOperations {

        @Test
        void shouldReturnPageWithFixedSizeOfEleven() {
            User user = new User();
            user.setId(1L);
            int requestedPage = 2;
            String searchTerm = "search";
            Page<Client> emptyPage = new PageImpl<>(Collections.emptyList());

            when(clientRepository.findSearchClientByOwner_Id(eq(user.getId()),
                    eq(searchTerm),
                    any(PageRequest.class)))
                    .thenReturn(emptyPage);

            Page<Client> result = clientService.getClients(user, requestedPage, searchTerm);

            assertNotNull(result);
            verify(clientRepository).findSearchClientByOwner_Id(eq(user.getId()),
                    eq(searchTerm), pageRequestCaptor.capture());

            PageRequest capturedRequest = pageRequestCaptor.getValue();
            assertEquals(requestedPage, capturedRequest.getPageNumber());
            assertEquals(11, capturedRequest.getPageSize());
        }
    }

    @Nested
    @DisplayName("Update Operations")
    class UpdateOperations {

        @Test
        void shouldUpdateClientSuccessfullyWithEmailNormalization() {
            User owner = new User();
            owner.setId(1L);
            Client existingClient = new Client();
            existingClient.setId(10L);
            existingClient.setOwner(owner);
            existingClient.setEmail("old@email.com");
            existingClient.setPhone("000");

            ClientDTO dto = new ClientDTO("Updated Name", "NEW@EMAIL.COM", "111");

            when(clientRepository.findById(10L)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsClientByEmail(dto.email())).thenReturn(false);
            when(clientRepository.existsClientByPhone(dto.phone())).thenReturn(false);

            clientService.updateClient(10L, dto, owner);

            verify(clientRepository).save(clientCaptor.capture());
            Client capturedClient = clientCaptor.getValue();

            assertEquals("new@email.com", capturedClient.getEmail());
            assertEquals(dto.name(), capturedClient.getName());
            assertEquals(dto.phone(), capturedClient.getPhone());
        }

        @Test
        void shouldUpdateSuccessfullyWhenEmailIsSameString() {
            User owner = new User();
            owner.setId(1L);
            Client existingClient = new Client();
            existingClient.setId(10L);
            existingClient.setOwner(owner);
            existingClient.setEmail("same@email.com");

            ClientDTO dto = new ClientDTO("Name", "same@email.com", "111");

            when(clientRepository.findById(10L)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsClientByEmail(dto.email())).thenReturn(true);
            when(clientRepository.existsClientByPhone(dto.phone())).thenReturn(false);

            assertDoesNotThrow(() -> clientService.updateClient(10L, dto, owner));

            verify(clientRepository).save(any(Client.class));
        }

        @Test
        void shouldThrowEmailAlreadyExistsWhenEmailIsDifferentAndExists() {
            User owner = new User();
            owner.setId(1L);
            Client existingClient = new Client();
            existingClient.setId(10L);
            existingClient.setOwner(owner);
            existingClient.setEmail("current@email.com");

            ClientDTO dto = new ClientDTO("Name", "other@email.com", "111");

            when(clientRepository.findById(10L)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsClientByEmail(dto.email())).thenReturn(true);

            assertThrows(EmailAlreadyExistsException.class,
                    () -> clientService.updateClient(10L, dto, owner));

            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        void shouldThrowPhoneAlreadyExistsWhenPhoneIsDifferentAndExists() {
            User owner = new User();
            owner.setId(1L);
            Client existingClient = new Client();
            existingClient.setId(10L);
            existingClient.setOwner(owner);
            existingClient.setEmail("email@email.com");
            existingClient.setPhone("123");

            ClientDTO dto = new ClientDTO("Name", "email@email.com", "456");

            when(clientRepository.findById(10L)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsClientByEmail(dto.email())).thenReturn(false);
            when(clientRepository.existsClientByPhone(dto.phone())).thenReturn(true);

            assertThrows(PhoneAlreadyExistsException.class,
                    () -> clientService.updateClient(10L, dto, owner));

            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        void shouldThrowUserIsNotOwner() {
            User owner = new User();
            owner.setId(1L);
            User intruder = new User();
            intruder.setId(2L);

            Client existingClient = new Client();
            existingClient.setId(10L);
            existingClient.setOwner(owner);
            existingClient.setEmail("any@email.com");
            existingClient.setPhone("111");

            ClientDTO dto = new ClientDTO("Name", "any@email.com", "111");

            when(clientRepository.findById(10L)).thenReturn(Optional.of(existingClient));
            when(clientRepository.existsClientByEmail(dto.email())).thenReturn(false);
            when(clientRepository.existsClientByPhone(dto.phone())).thenReturn(false);

            assertThrows(UserIsNotOwnerClientException.class,
                    () -> clientService.updateClient(10L, dto, intruder));

            verify(clientRepository, never()).save(any(Client.class));
        }

        @Test
        void shouldThrowClientNotFound() {
            User user = new User();
            ClientDTO dto = new ClientDTO("Name", "email", "phone");

            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ClientNotFoundException.class,
                    () -> clientService.updateClient(99L, dto, user));

            verify(clientRepository, never()).save(any(Client.class));
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        void shouldDeleteClientSuccessfully() {
            User owner = new User();
            owner.setId(1L);
            Client client = new Client();
            client.setId(10L);
            client.setOwner(owner);

            when(clientRepository.findById(10L)).thenReturn(Optional.of(client));

            clientService.deleteClient(10L, owner);

            verify(clientRepository).delete(client);
        }

        @Test
        void shouldThrowUserIsNotOwner() {
            User owner = new User();
            owner.setId(1L);
            User intruder = new User();
            intruder.setId(2L);

            Client client = new Client();
            client.setId(10L);
            client.setOwner(owner);

            when(clientRepository.findById(10L)).thenReturn(Optional.of(client));

            assertThrows(UserIsNotOwnerClientException.class,
                    () -> clientService.deleteClient(10L, intruder));

            verify(clientRepository, never()).delete(any(Client.class));
        }

        @Test
        void shouldThrowClientNotFound() {
            User user = new User();
            when(clientRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ClientNotFoundException.class,
                    () -> clientService.deleteClient(99L, user));

            verify(clientRepository, never()).delete(any(Client.class));
        }
    }
}