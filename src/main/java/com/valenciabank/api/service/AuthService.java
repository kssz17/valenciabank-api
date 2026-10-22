package com.valenciabank.api.service;

import com.valenciabank.api.dto.RegisterRequest;
import com.valenciabank.api.dto.RegisterResponse;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.Role;
import com.valenciabank.api.model.User;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class AuthService {

    // Regalo de bienvenida en dinero de juguete, para que la demo sea usable al instante.
    private static final BigDecimal SALDO_INICIAL = new BigDecimal("1000.00");

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    // Inyeccion por constructor: campos final, facil de testear, sin reflexion.
    public AuthService(UserRepository userRepository,
                       AccountRepository accountRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Crear usuario + cuenta debe ser atomico: o se crean los dos, o ninguno (rollback).
    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalStateException("El email ya esta registrado");
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
