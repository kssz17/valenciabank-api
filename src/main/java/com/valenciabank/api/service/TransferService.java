package com.valenciabank.api.service;

import com.valenciabank.api.dto.TransferRequest;
import com.valenciabank.api.dto.TransferResponse;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.Transaction;
import com.valenciabank.api.model.TxType;
import com.valenciabank.api.model.User;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.TransactionRepository;
import com.valenciabank.api.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

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

    // @Transactional: restar y sumar son UNA unidad. Si algo falla, rollback total.
    @Transactional
    public TransferResponse transfer(String email, TransferRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        Account origen = accountRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new IllegalStateException("La cuenta origen no existe"));
        Account destino = accountRepository.findByAccountNumber(request.toAccountNumber())
                .orElseThrow(() -> new IllegalStateException("La cuenta destino no existe"));

        if (origen.getId().equals(destino.getId())) {
            throw new IllegalStateException("No puedes transferirte a ti mismo");
        }
        // compareTo (no equals): compara el valor real del dinero, ignorando la escala.
        if (origen.getBalance().compareTo(request.amount()) < 0) {
            throw new IllegalStateException("Saldo insuficiente");
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
        transactionRepository.save(tx);

        return new TransferResponse(origen.getAccountNumber(), destino.getAccountNumber(),
                request.amount(), origen.getBalance());
    }
}
