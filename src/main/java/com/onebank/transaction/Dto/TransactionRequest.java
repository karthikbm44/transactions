package com.onebank.transaction.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransactionRequest {

    private BigDecimal debitAmount;
    private BigDecimal creditAmount;
    private String accountNumber;
    private String currentBalance;
}
