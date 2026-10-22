package com.valenciabank.api.exception;

// Intento de registrar un email que ya existe. -> 409 (conflicto).
public class DuplicateEmailException extends RuntimeException {
    public DuplicateEmailException(String message) {
        super(message);
    }
}
