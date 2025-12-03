package com.bingbank.cardsservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TransactionFilterRequest {
    private Long cardId;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private String category;
    private String merchantName;
    private String transactionType;
    private Integer limit;
    private String sortBy; // date, amount, merchant
    private String sortDirection; // asc, desc
}