package com.valenciabank.api.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI valenciaBankOpenAPI() {
        // El esquema "bearerAuth" anade el boton "Authorize" en Swagger para pegar el JWT.
        final String scheme = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("ValenciaBank API")
                        .version("1.0")
                        .description("API REST de un banco simulado: registro, login con JWT, "
                                + "saldo, historial y transferencias con integridad transaccional. "
                                + "Dinero de juguete, demo."))
                .addSecurityItem(new SecurityRequirement().addList(scheme))
                .components(new Components().addSecuritySchemes(scheme,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
