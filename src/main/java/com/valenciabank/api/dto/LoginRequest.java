package com.valenciabank.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Email(message = "El email no es valido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        String password
) {
}
