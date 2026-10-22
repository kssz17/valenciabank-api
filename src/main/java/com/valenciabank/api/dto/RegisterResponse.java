package com.valenciabank.api.dto;

import java.math.BigDecimal;

// DTO de salida: lo que devolvemos al cliente. Nunca exponemos la entidad User
// directamente (asi jamas se escapa el hash de la contrasena).
public record RegisterResponse(
        Long userId,
        String email,
        String accountNumber,
        BigDecimal balance
) {
}
