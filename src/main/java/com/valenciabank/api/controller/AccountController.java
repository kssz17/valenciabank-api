package com.valenciabank.api.controller;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.dto.TransactionResponse;
import com.valenciabank.api.service.AccountService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // El filtro ya identifico al usuario; aqui recogemos su email del contexto de seguridad.
    @GetMapping("/me")
    public ResponseEntity<AccountResponse> myAccount(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(accountService.getMyAccount(email));
    }

    // Por defecto: 10 por pagina, mas recientes primero. El cliente puede cambiar ?page= y ?size=.
    @GetMapping("/me/transactions")
    public ResponseEntity<Page<TransactionResponse>> myTransactions(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(accountService.getMyTransactions(authentication.getName(), pageable));
    }
}
