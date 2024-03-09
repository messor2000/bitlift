package com.example.backend.error;

public class PasswordMissmatchException extends RuntimeException{
    public PasswordMissmatchException(String message) {
        super(message);
    }
}
