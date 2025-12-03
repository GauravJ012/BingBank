package com.bingbank.cardsservice.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Data
public class ChangePinRequest {
    private Long customerId;
    private Long cardId;
    
    @NotBlank(message = "New PIN is required")
    @Pattern(regexp = "^\\d{4}$", message = "PIN must be 4 digits")
    private String newPin;
}