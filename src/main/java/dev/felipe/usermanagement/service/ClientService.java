package dev.felipe.usermanagement.service;

import dev.felipe.usermanagement.dto.ClientDTO;
import dev.felipe.usermanagement.exception.EmailAlreadyExistsException;
import dev.felipe.usermanagement.exception.PhoneAlreadyExistsException;
import dev.felipe.usermanagement.model.Client;
import dev.felipe.usermanagement.model.User;
import dev.felipe.usermanagement.repository.ClientRepository;
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
}
