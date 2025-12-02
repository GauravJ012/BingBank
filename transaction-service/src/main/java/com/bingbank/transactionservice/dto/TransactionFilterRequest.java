package com.bingbank.transactionservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionFilterRequest {
    private String accountNumber;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String transactionType; // DEBIT or CREDIT
    private String otherAccountNumber; // Filter by source or target account
    private Integer limit;
    private String sortBy; // transactionDate, amount, transactionType
    private String sortDirection; // ASC or DESC
}