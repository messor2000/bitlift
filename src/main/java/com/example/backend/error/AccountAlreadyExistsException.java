package com.example.backend.error;

public class AccountAlreadyExistsException extends RuntimeException {
    public AccountAlreadyExistsException(String message) {
        super(message);
    }
}
