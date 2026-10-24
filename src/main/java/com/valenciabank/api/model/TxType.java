package com.valenciabank.api.model;

public enum TxType {
    TRANSFER,   // transferencia entre dos cuentas del banco
    DEPOSIT,    // ingreso: el dinero entra desde fuera (sin cuenta origen)
    WITHDRAWAL  // retiro: el dinero sale fuera del banco (sin cuenta destino)
}
