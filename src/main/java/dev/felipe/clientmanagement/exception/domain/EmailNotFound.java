package dev.felipe.clientmanagement.exception.domain;

public class EmailNotFound extends RuntimeException {

    public EmailNotFound() {
        super("Usuário inexistente. Tente outro ou registre-se.");
    }
}
