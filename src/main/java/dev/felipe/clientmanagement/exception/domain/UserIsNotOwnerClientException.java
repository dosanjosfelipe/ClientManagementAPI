package dev.felipe.clientmanagement.exception.domain;

public class UserIsNotOwnerClientException extends RuntimeException {
    public UserIsNotOwnerClientException(String message) {
        super(message);
    }
}
