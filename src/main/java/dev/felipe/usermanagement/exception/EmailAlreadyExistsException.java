package dev.felipe.usermanagement.exception;

public class EmailAlreadyExistsException extends RuntimeException {

    public EmailAlreadyExistsException() {
        super("Esse email já foi registrado.");
    }
}
