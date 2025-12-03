package com.bingbank.cardsservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CreditCardDTO {
    private Long cardId;
    private Long customerId;
    private String accountNumber;
    private String cardNumber;
    private String cardholderName;
    private LocalDate expiryDate;
    private String cvv;
    private BigDecimal creditLimit;
    private BigDecimal availableCredit;
    private BigDecimal outstandingBalance;
    private BigDecimal usedCredit;
    private LocalDate paymentDueDate;
    private String cardStatus;
    private String expiryMonth;
    private String expiryYear;
    private Integer daysUntilDue;
}