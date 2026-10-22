package com.valenciabank.api.controller;

import com.valenciabank.api.dto.TransferRequest;
import com.valenciabank.api.dto.TransferResponse;
import com.valenciabank.api.service.TransferService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<TransferResponse> transfer(Authentication authentication,
                                                     @Valid @RequestBody TransferRequest request) {
        return ResponseEntity.ok(transferService.transfer(authentication.getName(), request));
    }
}
