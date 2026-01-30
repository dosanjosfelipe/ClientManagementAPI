package dev.felipe.clientmanagement.exception.domain;

public class ClientNotFoundException extends RuntimeException {
    public ClientNotFoundException() {
        super("Cliente não encontrado.");
    }
}
