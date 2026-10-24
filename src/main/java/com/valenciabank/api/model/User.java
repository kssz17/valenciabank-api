package com.valenciabank.api.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users") // "user" es palabra reservada en varios motores SQL; usamos "users".
@Getter
@Setter
@NoArgsConstructor // JPA instancia la entidad por reflexion y necesita un constructor vacio.
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT de MySQL.
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "full_name")
    private String fullName;

    private String locality;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    // STRING guarda "ROLE_USER" (legible y estable). Con ORDINAL se guardaria 0/1,
    // y reordenar el enum corromperia los datos existentes.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
