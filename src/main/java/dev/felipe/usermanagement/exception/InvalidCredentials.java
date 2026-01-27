package dev.felipe.usermanagement.exception;

public class InvalidCredentials extends RuntimeException {

    public InvalidCredentials() {
        super("Senha incorreta. Tente outra.");
    }
}
