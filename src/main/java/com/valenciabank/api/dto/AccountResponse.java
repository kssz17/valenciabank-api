package com.valenciabank.api.dto;

import java.math.BigDecimal;

public record AccountResponse(String accountNumber, BigDecimal balance) {
}
