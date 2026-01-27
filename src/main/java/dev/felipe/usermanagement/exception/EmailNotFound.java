package dev.felipe.usermanagement.exception;

public class EmailNotFound extends RuntimeException {

    public EmailNotFound() {
        super("Usuário inexistente. Tente outro ou registre-se.");
    }
}
