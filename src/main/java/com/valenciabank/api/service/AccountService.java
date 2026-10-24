package com.valenciabank.api.service;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.dto.AmountRequest;
import com.valenciabank.api.dto.CategorySummaryResponse;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Account account = cuentaDe(email);
        return transactionRepository
                .findByFromAccount_IdOrToAccount_Id(account.getId(), account.getId(), pageable)
                .map(tx -> new TransactionResponse(
                        tx.getId(),
                        tx.getFromAccount() != null ? tx.getFromAccount().getAccountNumber() : null,
                        tx.getToAccount() != null ? tx.getToAccount().getAccountNumber() : null,
                        tx.getAmount(),
                        tx.getType().name(),
                        tx.getCategory(),
                        tx.getDescription(),
                        tx.getCreatedAt()));
    }

    // Control de gastos: total retirado por categoria, de mayor a menor.
    @Transactional(readOnly = true)
    public List<CategorySummaryResponse> getSpendingByCategory(String email) {
        Account account = cuentaDe(email);
        List<Transaction> gastos = transactionRepository
                .findByFromAccount_IdAndType(account.getId(), TxType.WITHDRAWAL);

        Map<String, List<Transaction>> porCategoria = gastos.stream()
                .collect(Collectors.groupingBy(t -> t.getCategory() != null ? t.getCategory() : "OTROS"));

        return porCategoria.entrySet().stream()
                .map(e -> new CategorySummaryResponse(
                        e.getKey(),
                        e.getValue().stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add),
                        e.getValue().size()))
                .sorted(Comparator.comparing(CategorySummaryResponse::total).reversed())
                .toList();
    }

    // Demo: el usuario ingresa dinero de juguete a su propia cuenta.
    @Transactional
    public AccountResponse deposit(String email, AmountRequest request) {
        Account account = cuentaBloqueada(email);
        account.setBalance(account.getBalance().add(request.amount()));
        accountRepository.save(account);
        registrar(null, account, request.amount(), TxType.DEPOSIT,
                request.category() != null ? request.category() : "INGRESO", request.description());
        log.info("Ingreso propio: {} en {} (saldo {})", request.amount(), account.getAccountNumber(), account.getBalance());
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    // Demo: el usuario retira/gasta de su cuenta (el dinero sale fuera).
    @Transactional
    public AccountResponse withdraw(String email, AmountRequest request) {
        Account account = cuentaBloqueada(email);
        if (account.getBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }
        account.setBalance(account.getBalance().subtract(request.amount()));
        accountRepository.save(account);
        registrar(account, null, request.amount(), TxType.WITHDRAWAL, request.category(), request.description());
        log.info("Retiro: {} de {} (saldo {})", request.amount(), account.getAccountNumber(), account.getBalance());
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    private User usuario(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private Account cuentaDe(String email) {
        User user = usuario(email);
        return accountRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
    }

    // Carga la cuenta del usuario con bloqueo (FOR UPDATE), como en la transferencia.
    private Account cuentaBloqueada(String email) {
        User user = usuario(email);
        Long accountId = accountRepository.findIdByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
        return accountRepository.findByIdForUpdate(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
    }

    private void registrar(Account from, Account to, BigDecimal amount, TxType type, String category, String description) {
        Transaction tx = new Transaction();
        tx.setFromAccount(from);
        tx.setToAccount(to);
        tx.setAmount(amount);
        tx.setType(type);
        tx.setCategory(category);
        tx.setDescription(description);
        transactionRepository.save(tx);
    }
}
