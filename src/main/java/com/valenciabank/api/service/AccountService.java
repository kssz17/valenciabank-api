package com.valenciabank.api.service;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.dto.TransactionResponse;
import com.valenciabank.api.exception.ResourceNotFoundException;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.User;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.TransactionRepository;
import com.valenciabank.api.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

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
    public AccountResponse getMyAccount(String email) {
        Account account = cuentaDe(email);
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getMyTransactions(String email, Pageable pageable) {
        Account account = cuentaDe(email);
        return transactionRepository
                .findByFromAccount_IdOrToAccount_Id(account.getId(), account.getId(), pageable)
                .map(tx -> new TransactionResponse(
                        tx.getId(),
                        tx.getFromAccount() != null ? tx.getFromAccount().getAccountNumber() : null,
                        tx.getToAccount().getAccountNumber(),
                        tx.getAmount(),
                        tx.getType().name(),
                        tx.getCreatedAt()));
    }

    private Account cuentaDe(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        return accountRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta no existe"));
    }
}
