package com.bingbank.cardsservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class CreditCardTransactionDTO {
    private Long transactionId;
    private Long cardId;
    private String cardNumber;
    private LocalDate transactionDate;
    private LocalTime transactionTime;
    private String merchantName;
    private String category;
    private BigDecimal amount;
    private String transactionType;
    private String status;
}