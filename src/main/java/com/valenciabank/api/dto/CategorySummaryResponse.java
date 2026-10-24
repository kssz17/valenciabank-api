package com.valenciabank.api.dto;

import java.math.BigDecimal;

// Total gastado por categoria (para el control de gastos).
public record CategorySummaryResponse(
        String category,
        BigDecimal total,
        long count
) {
}
