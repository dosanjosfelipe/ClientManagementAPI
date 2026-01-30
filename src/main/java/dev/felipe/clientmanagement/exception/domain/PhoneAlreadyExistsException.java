package dev.felipe.usermanagement.exception.domain;

public class PhoneAlreadyExistsException extends RuntimeException {
    public PhoneAlreadyExistsException() {
        super("Esse email já foi registrado.");
    }
}
