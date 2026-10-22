package com.valenciabank.api.exception;

// Algo que se busco no existe (cuenta, usuario...). -> 404.
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
