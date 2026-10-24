package com.valenciabank.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TransferRequest(

        @NotBlank(message = "La cuenta destino es obligatoria")
        String toAccountNumber,

        @NotNull(message = "El importe es obligatorio")
        @DecimalMin(value = "0.01", message = "El importe debe ser positivo")
        @Digits(integer = 17, fraction = 2, message = "Maximo 2 decimales")
        BigDecimal amount,

        String description
) {
}
