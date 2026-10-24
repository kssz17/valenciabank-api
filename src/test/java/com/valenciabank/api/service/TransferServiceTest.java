package com.valenciabank.api.service;

import com.valenciabank.api.dto.TransferRequest;
import com.valenciabank.api.dto.TransferResponse;
import com.valenciabank.api.exception.BusinessException;
import com.valenciabank.api.exception.ResourceNotFoundException;
import com.valenciabank.api.model.Account;
import com.valenciabank.api.model.User;
import com.valenciabank.api.repository.AccountRepository;
import com.valenciabank.api.repository.TransactionRepository;
import com.valenciabank.api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Tests UNITARIOS de la transferencia: sin BD, sin servidor. Mockito finge los repositorios
// para probar SOLO la logica. Patron AAA: Arrange (preparar), Act (actuar), Assert (comprobar).
@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private TransferService transferService;

    private User ana;
    private Account cuentaAna;
    private Account cuentaLuis;

    @BeforeEach
    void setUp() {
        ana = new User();
        ana.setId(1L);
        ana.setEmail("ana@demo.com");

        cuentaAna = new Account();
        cuentaAna.setId(1L);
        cuentaAna.setAccountNumber("ES-ANA");
        cuentaAna.setBalance(new BigDecimal("1000.00"));

        cuentaLuis = new Account();
        cuentaLuis.setId(2L);
        cuentaLuis.setAccountNumber("ES-LUIS");
        cuentaLuis.setBalance(new BigDecimal("500.00"));
    }

    @Test
    void transferencia_correcta_mueve_el_dinero_exacto() {
        when(userRepository.findByEmail("ana@demo.com")).thenReturn(Optional.of(ana));
        when(accountRepository.findIdByUserId(1L)).thenReturn(Optional.of(1L));
        when(accountRepository.findIdByAccountNumber("ES-LUIS")).thenReturn(Optional.of(2L));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(cuentaAna));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(cuentaLuis));

        TransferResponse resp = transferService.transfer("ana@demo.com",
                new TransferRequest("ES-LUIS", new BigDecimal("300.00"), null));

        assertThat(cuentaAna.getBalance()).isEqualByComparingTo("700.00");
        assertThat(cuentaLuis.getBalance()).isEqualByComparingTo("800.00");
        assertThat(resp.remainingBalance()).isEqualByComparingTo("700.00");
        verify(transactionRepository).save(any());
    }

    @Test
    void saldo_insuficiente_lanza_excepcion_y_no_mueve_dinero() {
        cuentaAna.setBalance(new BigDecimal("100.00"));
        when(userRepository.findByEmail("ana@demo.com")).thenReturn(Optional.of(ana));
        when(accountRepository.findIdByUserId(1L)).thenReturn(Optional.of(1L));
        when(accountRepository.findIdByAccountNumber("ES-LUIS")).thenReturn(Optional.of(2L));
        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(cuentaAna));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(cuentaLuis));

        assertThatThrownBy(() -> transferService.transfer("ana@demo.com",
                new TransferRequest("ES-LUIS", new BigDecimal("300.00"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Saldo insuficiente");

        assertThat(cuentaAna.getBalance()).isEqualByComparingTo("100.00");
        assertThat(cuentaLuis.getBalance()).isEqualByComparingTo("500.00");
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void no_puedo_transferirme_a_mi_mismo() {
        when(userRepository.findByEmail("ana@demo.com")).thenReturn(Optional.of(ana));
        when(accountRepository.findIdByUserId(1L)).thenReturn(Optional.of(1L));
        when(accountRepository.findIdByAccountNumber("ES-ANA")).thenReturn(Optional.of(1L));

        assertThatThrownBy(() -> transferService.transfer("ana@demo.com",
                new TransferRequest("ES-ANA", new BigDecimal("50.00"), null)))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("ti mismo");

        verify(transactionRepository, never()).save(any());
    }

    @Test
    void cuenta_destino_inexistente_lanza_not_found() {
        when(userRepository.findByEmail("ana@demo.com")).thenReturn(Optional.of(ana));
        when(accountRepository.findIdByUserId(1L)).thenReturn(Optional.of(1L));
        when(accountRepository.findIdByAccountNumber("ES-XXX")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> transferService.transfer("ana@demo.com",
                new TransferRequest("ES-XXX", new BigDecimal("50.00"), null)))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(transactionRepository, never()).save(any());
    }
}
