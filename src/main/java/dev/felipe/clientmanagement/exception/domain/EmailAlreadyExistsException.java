package dev.felipe.clientmanagement.exception.domain;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("Esse email já foi registrado.");
    }
}
