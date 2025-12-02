package com.bingbank.transactionservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionDTO {
    private Long transactionId;
    private BigDecimal amount;
    private String transactionType;
    private LocalDate transactionDate;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private String accountNumber;
}