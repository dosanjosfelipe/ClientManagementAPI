package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.ClientRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;
import dev.felipe.clientmanagement.model.Client;
import org.springframework.transaction.annotation.Transactional;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

@Service
public class ClientFilesService {

    private final ClientRepository clientRepository;

    public ClientFilesService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional(readOnly = true)
    public void generateCSV(User user, OutputStream outputStream) throws IOException {

        Stream<Client> clients = clientRepository.streamAllByOwnerId(user.getId());

        BufferedWriter writer =
                new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
        CSVFormat format = CSVFormat.DEFAULT.builder().build();

        CSVPrinter printer = new CSVPrinter(writer, format);
        printer.printRecord("Nome", "Email", "Telefone", "Criado em", "Atualizado em");

        try {
            clients.forEach(client -> {
                try {
                    printer.printRecord(
                            client.getName(),
                            client.getEmail(),
                            client.getPhone(),
                            client.getCreatedAt().toLocalDate(),
                            client.getUpdatedAt().toLocalDate()
                    );
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });

            printer.flush();
        } finally {
            clients.close();
        }
    }
}
