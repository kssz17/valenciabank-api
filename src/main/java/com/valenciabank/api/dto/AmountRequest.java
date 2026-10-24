package com.valenciabank.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

// Importe para ingresar o retirar.
public record AmountRequest(

        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01", message = "El importe debe ser positivo")
        @Digits(integer = 17, fraction = 2, message = "Maximo 2 decimales")
        BigDecimal amount
) {
}
