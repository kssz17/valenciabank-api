package com.valenciabank.api.controller;

import com.valenciabank.api.dto.AccountResponse;
import com.valenciabank.api.dto.DepositRequest;
import com.valenciabank.api.dto.UserSummaryResponse;
import com.valenciabank.api.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserSummaryResponse>> listUsers() {
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PostMapping("/deposit")
    public ResponseEntity<AccountResponse> deposit(@Valid @RequestBody DepositRequest request) {
        return ResponseEntity.ok(adminService.deposit(request));
    }
}
