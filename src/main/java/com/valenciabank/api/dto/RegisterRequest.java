package com.valenciabank.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// DTO de entrada. Es un 'record': inmutable y conciso. No es una entidad:
// solo transporta lo que llega en la peticion, con sus reglas de validacion.
public record RegisterRequest(

        @Email(message = "El email no es valido")
        @NotBlank(message = "El email es obligatorio")
        String email,

        @NotBlank(message = "La contrasena es obligatoria")
        @Size(min = 8, message = "La contrasena debe tener al menos 8 caracteres")
        String password,

        // Opcionales: dan vida al perfil del usuario.
        String fullName,
        String locality
) {
}
