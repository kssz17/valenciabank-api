package com.valenciabank.api.dto;

// Lo que devuelve el login: el token que el cliente guardara y enviara en cada peticion.
public record AuthResponse(String token) {
}
