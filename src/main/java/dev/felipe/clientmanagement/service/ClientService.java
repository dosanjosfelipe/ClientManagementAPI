package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.dto.client.ClientDTO;
import dev.felipe.clientmanagement.exception.domain.ClientNotFoundException;
import dev.felipe.clientmanagement.exception.domain.EmailAlreadyExistsException;
import dev.felipe.clientmanagement.exception.domain.PhoneAlreadyExistsException;
import dev.felipe.clientmanagement.exception.domain.UserIsNotOwnerClientException;
import dev.felipe.clientmanagement.model.Client;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.ClientRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.Objects;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void saveClient(ClientDTO dto, User user) {

        if (clientRepository.existsClientByEmail(dto.email())) {
            throw new EmailAlreadyExistsException("Esse email já existe em outro cliente.");
        }

        if (clientRepository.existsClientByPhone(dto.phone())) {
            throw new PhoneAlreadyExistsException("Esse telefone já existe em outro cliente.");
        }

        Client client = new Client();

        client.setName(dto.name());
        client.setEmail(dto.email().toLowerCase());
        client.setPhone(dto.phone());
        client.setOwner(user);

        clientRepository.save(client);
    }

    public Page<Client> getClients(User user, int page, String search) {

        // pageSize = 11 devido a melhor visibilidade no frontend
        PageRequest pageRequest = PageRequest.of(page, 11);

        return clientRepository.findSearchClientByOwner_Id(user.getId(), search, pageRequest);

    }

    public void updateClient(Long clientId, ClientDTO dto, User user) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Esse cliente não existe."));

        if (clientRepository.existsClientByEmail(dto.email()) &&
                !dto.email().equals(client.getEmail())) {
            throw new EmailAlreadyExistsException("Esse email já existe em outro cliente.");
        }

        if (clientRepository.existsClientByPhone(dto.phone()) &&
                !dto.phone().equals(client.getPhone())) {
            throw new PhoneAlreadyExistsException("Esse telefone já existe em outro cliente.");
        }

        if (!Objects.equals(user.getId(), client.getOwner().getId())) {
            throw new UserIsNotOwnerClientException(
                    "Esse usuário não tem permissões para esse cliente.");
        }

        client.setName(dto.name());
        client.setEmail(dto.email().toLowerCase());
        client.setPhone(dto.phone());

        clientRepository.save(client);
    }

    public void deleteClient(Long clientId, User user) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new ClientNotFoundException("Esse cliente não existe."));

        if (!Objects.equals(user.getId(), client.getOwner().getId())) {
            throw new UserIsNotOwnerClientException(
                    "Esse usuário não tem permissões para esse cliente.");
        }

        clientRepository.delete(client);
    }
}
