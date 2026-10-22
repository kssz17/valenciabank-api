package com.valenciabank.api.dto;

import java.time.LocalDateTime;

// Vista de un usuario para el admin. Sin password_hash: nunca sale de la entidad.
public record UserSummaryResponse(
        Long id,
        String email,
        String role,
        LocalDateTime createdAt
) {
}
