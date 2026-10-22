package com.valenciabank.api.dto;

import java.math.BigDecimal;

public record TransferResponse(
        String fromAccountNumber,
        String toAccountNumber,
        BigDecimal amount,
        BigDecimal remainingBalance
) {
}
