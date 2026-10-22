package com.valenciabank.api.service;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.dto.DepositRequest;
import com.valenciabank.api.dto.UserSummaryResponse;
import com.valenciabank.api.exception.ResourceNotFoundException;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.Transaction;
import com.valenciabank.api.model.TxType;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.TransactionRepository;
import com.valenciabank.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private static final Logger log = LoggerFactory.getLogger(AdminService.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public AdminService(UserRepository userRepository,
                        AccountRepository accountRepository,
                        TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public List<UserSummaryResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummaryResponse(u.getId(), u.getEmail(), u.getRole().name(), u.getCreatedAt()))
                .toList();
    }

    // Ingreso simulado: sumar al saldo y registrar el movimiento. Las dos cosas o ninguna.
    @Transactional
    public AccountResponse deposit(DepositRequest request) {
        Account account = accountRepository.findByAccountNumber(request.accountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));

        // BigDecimal.add: suma exacta, nunca aritmetica de double.
        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);

        Transaction tx = new Transaction();
        tx.setFromAccount(null);     // ingreso: el dinero entra desde fuera, sin cuenta origen
        tx.setToAccount(account);
        tx.setAmount(request.amount());
        tx.setType(TxType.DEPOSIT);
        transactionRepository.save(tx);

        log.info("Ingreso: {} a la cuenta {} (nuevo saldo {})",
                request.amount(), account.getAccountNumber(), account.getBalance());
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }
}
