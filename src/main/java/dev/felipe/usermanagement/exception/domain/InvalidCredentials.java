package dev.felipe.usermanagement.exception.domain;

public class InvalidCredentials extends RuntimeException {

    public InvalidCredentials() {
        super("Senha incorreta. Tente outra.");
    }
}
