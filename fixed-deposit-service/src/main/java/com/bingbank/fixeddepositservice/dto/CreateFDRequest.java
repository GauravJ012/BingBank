package com.bingbank.fixeddepositservice.dto;

import lombok.Data;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class CreateFDRequest {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    
    @NotNull(message = "Account number is required")
    private String accountNumber;
    
    @NotNull(message = "Principal amount is required")
    @Min(value = 100, message = "Minimum FD amount is $100")
    private BigDecimal principalAmount;
    
    @NotNull(message = "Tenure is required")
    @Min(value = 1, message = "Minimum tenure is 1 year")
    private Integer tenureYears;
}