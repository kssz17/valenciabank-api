package com.valenciabank.api.controller;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.dto.AmountRequest;
import com.valenciabank.api.dto.ProfileResponse;
import com.valenciabank.api.dto.TransactionResponse;
import com.valenciabank.api.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> myProfile(Authentication authentication) {
        return ResponseEntity.ok(accountService.getMyProfile(authentication.getName()));
    }

    // Por defecto: 10 por pagina, mas recientes primero. El cliente puede cambiar ?page= y ?size=.
    @GetMapping("/me/transactions")
    public ResponseEntity<Page<TransactionResponse>> myTransactions(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(accountService.getMyTransactions(authentication.getName(), pageable));
    }

    @PostMapping("/me/deposit")
    public ResponseEntity<AccountResponse> deposit(Authentication authentication,
                                                   @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.deposit(authentication.getName(), request.amount()));
    }

    @PostMapping("/me/withdraw")
    public ResponseEntity<AccountResponse> withdraw(Authentication authentication,
                                                    @Valid @RequestBody AmountRequest request) {
        return ResponseEntity.ok(accountService.withdraw(authentication.getName(), request.amount()));
    }
}
