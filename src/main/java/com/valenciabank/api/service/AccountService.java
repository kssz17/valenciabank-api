package com.valenciabank.api.service;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.dto.ProfileResponse;
import com.valenciabank.api.dto.TransactionResponse;
import com.valenciabank.api.exception.BusinessException;
import com.valenciabank.api.exception.ResourceNotFoundException;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.Transaction;
import com.valenciabank.api.model.TxType;
import com.valenciabank.api.model.User;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.TransactionRepository;
import com.valenciabank.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(AccountRepository accountRepository,
                          UserRepository userRepository,
                          TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String email) {
        User user = usuario(email);
        Account account = accountRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
        return new ProfileResponse(user.getEmail(), user.getFullName(), user.getLocality(),
                account.getAccountNumber(), account.getBalance());
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(String email, Pageable pageable) {
        User user = usuario(email);
        Account account = accountRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
        return transactionRepository
                .findByFromAccount_IdOrToAccount_Id(account.getId(), account.getId(), pageable)
                .map(tx -> new TransactionResponse(
                        tx.getId(),
                        tx.getFromAccount() != null ? tx.getFromAccount().getAccountNumber() : null,
                        tx.getToAccount() != null ? tx.getToAccount().getAccountNumber() : null,
                        tx.getAmount(),
                        tx.getType().name(),
                        tx.getCreatedAt()));
    }

    // Demo: el usuario ingresa dinero de juguete a su propia cuenta.
    @Transactional
    public AccountResponse deposit(String email, BigDecimal amount) {
        Account account = cuentaBloqueada(email);
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        registrar(null, account, amount, TxType.DEPOSIT);
        log.info("Ingreso propio: {} en {} (saldo {})", amount, account.getAccountNumber(), account.getBalance());
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    // Demo: el usuario retira dinero de su propia cuenta (el dinero sale fuera).
    @Transactional
    public AccountResponse withdraw(String email, BigDecimal amount) {
        Account account = cuentaBloqueada(email);
        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        registrar(account, null, amount, TxType.WITHDRAWAL);
        log.info("Retiro: {} de {} (saldo {})", amount, account.getAccountNumber(), account.getBalance());
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    private User usuario(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    // Carga la cuenta del usuario con bloqueo (FOR UPDATE), como en la transferencia.
    private Account cuentaBloqueada(String email) {
        User user = usuario(email);
        Long accountId = accountRepository.findIdByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
    }

    private void registrar(Account from, Account to, BigDecimal amount, TxType type) {
        Transaction tx = new Transaction();
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setType(type);
        transactionRepository.save(tx);
    }
}
