package dev.felipe.clientmanagement.exception.domain;

public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException() {
        super("Esse email já foi registrado.");
    }
}
