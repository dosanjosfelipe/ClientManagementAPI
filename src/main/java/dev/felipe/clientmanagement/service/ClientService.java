package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.dto.client.ClientDTO;
import dev.felipe.clientmanagement.exception.domain.ClientNotFoundException;
import dev.felipe.clientmanagement.exception.domain.EmailAlreadyExistsException;
import dev.felipe.clientmanagement.exception.domain.PhoneAlreadyExistsException;
import dev.felipe.clientmanagement.model.Client;
import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.ClientRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ClientService {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public void saveClient(ClientDTO dto, User user) {

        if (clientRepository.findByEmail(dto.email().toLowerCase()).isPresent()) {
            throw new EmailAlreadyExistsException();
        }

        if (clientRepository.findByPhone(dto.phone()).isPresent()) {
            throw new PhoneAlreadyExistsException();
        }

        Client client = new Client();

        client.setName(dto.name());
        client.setEmail(dto.email().toLowerCase());
        client.setPhone(dto.phone());
        client.setOwner(user);

        clientRepository.save(client);
    }

    public List<Client> getClients(User user) {

        return clientRepository.getClientByOwner_Id(user.getId());
    }

    public void updateClient(Long clientId, ClientDTO dto) {

        Client client = clientRepository.findById(clientId)
                .orElseThrow(ClientNotFoundException::new);

        client.setName(dto.name());
        client.setEmail(dto.email().toLowerCase());
        client.setPhone(dto.phone());

        clientRepository.save(client);
    }

}
