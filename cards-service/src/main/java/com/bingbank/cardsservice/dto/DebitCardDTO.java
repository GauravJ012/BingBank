package com.bingbank.cardsservice.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class DebitCardDTO {
    private Long cardId;
    private Long customerId;
    private String accountNumber;
    private String cardNumber;
    private String cardholderName;
    private LocalDate expiryDate;
    private String cvv;
    private String cardStatus;
    private String expiryMonth;
    private String expiryYear;
}