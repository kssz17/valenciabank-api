package com.valenciabank.api.controller;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.service.AccountService;
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
}
