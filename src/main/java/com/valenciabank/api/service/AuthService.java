package com.valenciabank.api.service;

import com.valenciabank.api.dto.AuthResponse;
import com.valenciabank.api.dto.LoginRequest;
import com.valenciabank.api.dto.RegisterRequest;
import com.valenciabank.api.dto.RegisterResponse;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.Role;
import com.valenciabank.api.model.User;
import com.valenciabank.api.exception.DuplicateEmailException;
import com.valenciabank.api.exception.ResourceNotFoundException;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    // Regalo de bienvenida en dinero de juguete, para que la demo sea usable al instante.
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("1000.00");

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    // Inyeccion por constructor: campos final, facil de testear, sin reflexion.
    public AuthService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public AuthResponse login(LoginRequest request) {
        // Spring verifica email + contrasena (contra el hash BCrypt). Si falla, lanza excepcion.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        log.info("Login correcto: {}", user.getEmail());
        String token = jwtService.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token);
    }

    // Crear usuario + cuenta debe ser atomico: o se crean los dos, o ninguno (rollback).
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateEmailException("El email ya esta registrado");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password())); // BCrypt, nunca texto plano
        user.setRole(Role.ROLE_USER);
        user = userRepository.save(user);

        Account account = new Account();
        account.setAccountNumber(generarNumeroCuenta());
        account.setBalance(SALDO_INICIAL);
        account.setUser(user);
        account = accountRepository.save(account);

        log.info("Registro: usuario {} (cuenta {}) creado", user.getEmail(), account.getAccountNumber());
        return new RegisterResponse(user.getId(), user.getEmail(),
                account.getAccountNumber(), account.getBalance());
    }

    // IBAN simulado (dinero de juguete): "ES" + 22 digitos.
    private String generarNumeroCuenta() {
        StringBuilder sb = new StringBuilder("ES");
        for (int i = 0; i < 22; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }
}
