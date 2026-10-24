package com.valenciabank.api.repository;

import com.valenciabank.api.model.Transaction;
import com.valenciabank.api.model.TxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Movimientos donde la cuenta aparece como origen O como destino, por paginas.
    Page<Transaction> findByFromAccount_IdOrToAccount_Id(Long fromAccountId, Long toAccountId, Pageable pageable);

    // Los retiros (gastos) de una cuenta, para resumir por categoria.
    List<Transaction> findByFromAccount_IdAndType(Long accountId, TxType type);
}
