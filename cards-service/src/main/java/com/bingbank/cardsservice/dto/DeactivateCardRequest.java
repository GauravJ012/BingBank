package com.bingbank.cardsservice.dto;

import lombok.Data;

@Data
public class DeactivateCardRequest {
    private Long customerId;
    private Long cardId;
    private String cardType; // DEBIT or CREDIT
}