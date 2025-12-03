package com.bingbank.fundtransferservice.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotBlank;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotBlank(message = "Source account number is required")
    private String sourceAccountNumber;
    
    @NotBlank(message = "Target account number is required")
    private String targetAccountNumber;
    
    @NotNull(message = "Amount is required")
    @Min(value = 1, message = "Amount must be at least $1")
    private BigDecimal amount;
    
    private String remarks;
}