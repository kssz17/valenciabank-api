package com.valenciabank.api.exception;

// Se rompe una regla de negocio (saldo insuficiente, transferirse a si mismo...). -> 400.
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
