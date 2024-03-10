package com.example.backend.error;

public class OldPasswordMissmatchException extends RuntimeException {
    public OldPasswordMissmatchException(String message) {
        super(message);
    }
}
