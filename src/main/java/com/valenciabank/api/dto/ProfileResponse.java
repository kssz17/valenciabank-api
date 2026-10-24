package com.valenciabank.api.dto;

import java.math.BigDecimal;

// Lo que ve el dashboard: datos del perfil + cuenta y saldo.
public record ProfileResponse(
        String email,
        String fullName,
        String locality,
        String accountNumber,
        BigDecimal balance
) {
}
