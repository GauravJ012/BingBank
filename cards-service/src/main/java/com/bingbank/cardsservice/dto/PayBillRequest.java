package com.bingbank.cardsservice.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class PayBillRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Card ID is required")
    private Long cardId;
    
    @NotNull(message = "Account number is required")
    private String accountNumber;
    
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least $1")
    private BigDecimal amount;
}