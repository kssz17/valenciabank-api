package com.valenciabank.api.dto;

import java.time.LocalDateTime;

// Forma unica y limpia de todos los errores de la API (en vez de un volcado feo de Java).
public record ErrorResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message
) {
}
