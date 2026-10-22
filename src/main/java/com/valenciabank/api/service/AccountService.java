package com.valenciabank.api.service;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.User;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    // readOnly=true: es solo lectura, se lo decimos a la transaccion (mas eficiente).
    @Transactional(readOnly = true)
    public AccountResponse getMyAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        Account account = accountRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new IllegalStateException("La cuenta no existe"));
        return new AccountResponse(account.getAccountNumber(), account.getBalance());
    }
}
