package com.valenciabank.api.model;

public enum TxType {
    TRANSFER, // transferencia entre dos cuentas del banco
    DEPOSIT   // ingreso simulado: el admin inyecta saldo de prueba
}
