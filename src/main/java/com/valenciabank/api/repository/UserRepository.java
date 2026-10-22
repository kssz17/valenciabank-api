package com.valenciabank.api.repository;

import com.valenciabank.api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring Data JPA implementa esta interfaz en tiempo de ejecucion.
// El nombre del metodo define la consulta: findByEmail -> WHERE email = ?
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
