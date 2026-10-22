package com.valenciabank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// fromAccountNumber es null cuando el movimiento es un ingreso (DEPOSIT).
public record TransactionResponse(
        Long id,
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        String type,
        LocalDateTime createdAt
) {
}
