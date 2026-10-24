package com.valenciabank.api.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// fromAccountNumber es null en ingresos; toAccountNumber es null en retiros.
public record TransactionResponse(
        Long id,
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        String type,
        String category,
        String description,
        LocalDateTime createdAt
) {
}
