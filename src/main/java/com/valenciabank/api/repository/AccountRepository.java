package com.valenciabank.api.repository;

import com.valenciabank.api.model.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUser_Id(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    // Solo el id (sin cargar la entidad): asi la primera carga real es la bloqueada.
    @Query("select a.id from Account a where a.user.id = :userId")
    Optional<Long> findIdByUserId(@Param("userId") Long userId);

    @Query("select a.id from Account a where a.accountNumber = :accountNumber")
    Optional<Long> findIdByAccountNumber(@Param("accountNumber") String accountNumber);

    // SELECT ... FOR UPDATE: bloquea la fila hasta el commit. La siguiente transaccion que
    // quiera esta cuenta ESPERA, y al pasar lee el saldo ya actualizado.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}
