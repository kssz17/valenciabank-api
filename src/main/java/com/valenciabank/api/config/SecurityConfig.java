package com.valenciabank.api.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@Configuration
public class SecurityConfig {

    // El bean que hashea y verifica contrasenas. Un unico sitio, reutilizable.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // El AuthenticationManager usa nuestro UserDetailsService + el PasswordEncoder
    // para verificar las credenciales en el login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthFilter) throws Exception {
        http
                // API REST stateless: no usamos cookies de sesion, asi que CSRF no aplica.
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()      // registro y login son publicos
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll() // documentacion
                        .requestMatchers("/admin/**").hasRole("ADMIN") // solo ADMIN (busca la autoridad ROLE_ADMIN)
                        .anyRequest().authenticated())                // el resto exige estar autenticado
                // Respuestas JSON limpias cuando falla la seguridad (en vez de cuerpo vacio).
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> writeError(res, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado"))
                        .accessDeniedHandler((req, res, e) -> writeError(res, HttpServletResponse.SC_FORBIDDEN, "Acceso denegado")))
                // Nuestro filtro corre ANTES del de usuario/contrasena de Spring.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    private void writeError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"status\":" + status + ",\"error\":\"" + message + "\"}");
    }
}
