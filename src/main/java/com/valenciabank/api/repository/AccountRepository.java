package com.valenciabank.api.repository;

import com.valenciabank.api.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByUser_Id(Long userId);

    boolean existsByAccountNumber(String accountNumber);
}
