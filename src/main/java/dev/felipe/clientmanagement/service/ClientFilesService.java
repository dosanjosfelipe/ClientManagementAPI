package dev.felipe.clientmanagement.service;

import dev.felipe.clientmanagement.model.User;
import dev.felipe.clientmanagement.repository.ClientRepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.stereotype.Service;
import dev.felipe.clientmanagement.model.Client;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
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

    @Transactional
    public void importCSV(MultipartFile file, User user) throws IOException {

        if (file.isEmpty()) {
            throw new IllegalArgumentException("Arquivo vazio");
        }

        List<Client> clients = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .setTrim(true)
                .build();

        try (Reader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8));
             CSVParser parser = new CSVParser(reader, format)) {

            for (CSVRecord record : parser) {
                Client client = new Client();

                client.setOwner(user);
                client.setName(record.get("Nome"));
                client.setEmail(record.get("Email"));
                client.setPhone(record.get("Telefone"));

                LocalDate createdDate = LocalDate.parse(record.get("Criado em"), dateFormatter);
                LocalDateTime createdDateTime = createdDate.atStartOfDay();
                client.setCreatedAt(createdDateTime);

                LocalDate updatedDate = LocalDate.parse(record.get("Atualizado em"), dateFormatter);
                LocalDateTime updatedDateTime = updatedDate.atStartOfDay();
                client.setUpdatedAt(updatedDateTime);

                clients.add(client);
            }
        }

        try {
            clientRepository.saveAll(clients);
            clientRepository.flush();
        } catch (ConstraintViolationException e) {
            throw new ConstraintViolationException(
                    "Campos únicos duplicados: " + e.getConstraintName(),
                    e.getSQLException(),
                    e.getConstraintName()
            );
        }
    }
}
