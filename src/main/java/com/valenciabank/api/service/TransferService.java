package com.valenciabank.api.service;

import com.valenciabank.api.dto.TransferRequest;
import com.valenciabank.api.dto.TransferResponse;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.Transaction;
import com.valenciabank.api.model.TxType;
import com.valenciabank.api.model.User;
import com.valenciabank.api.exception.BusinessException;
import com.valenciabank.api.exception.ResourceNotFoundException;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.TransactionRepository;
import com.valenciabank.api.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransferService(UserRepository userRepository,
                           AccountRepository accountRepository,
                           TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Transactional
    public TransferResponse transfer(String email, TransferRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        Long origenId = accountRepository.findIdByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta origen no existe"));
        Long destinoId = accountRepository.findIdByAccountNumber(request.toAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException("La cuenta destino no existe"));

        if (origenId.equals(destinoId)) {
            throw new BusinessException("No puedes transferirte a ti mismo");
        }

        // Bloqueamos SIEMPRE en el mismo orden (id ascendente) -> nunca hay deadlock cruzado.
        Long firstId = Math.min(origenId, destinoId);
        Long secondId = Math.max(origenId, destinoId);
        Account first = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        Account second = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new ResourceNotFoundException("Cuenta no encontrada"));
        Account origen = origenId.equals(firstId) ? first : second;
        Account destino = origenId.equals(firstId) ? second : first;

        if (origen.getBalance().compareTo(request.amount()) < 0) {
            throw new BusinessException("Saldo insuficiente");
        }

        origen.setBalance(origen.getBalance().subtract(request.amount()));
        destino.setBalance(destino.getBalance().add(request.amount()));
        accountRepository.save(origen);
        accountRepository.save(destino);

        Transaction tx = new Transaction();
        tx.setFromAccount(origen);
        tx.setToAccount(destino);
        tx.setAmount(request.amount());
        tx.setType(TxType.TRANSFER);
        tx.setDescription(request.description());
        transactionRepository.save(tx);

        log.info("Transferencia: {} de {} a {} (saldo origen {})", request.amount(),
                origen.getAccountNumber(), destino.getAccountNumber(), origen.getBalance());
        return new TransferResponse(origen.getAccountNumber(), destino.getAccountNumber(),
                request.amount(), origen.getBalance());
    }
}
