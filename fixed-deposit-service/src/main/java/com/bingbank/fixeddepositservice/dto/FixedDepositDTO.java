package com.bingbank.fixeddepositservice.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class FixedDepositDTO {
    private Long fdId;
    private Long customerId;
    private String accountNumber;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private Integer tenureYears;
    private LocalDate startDate;
    private LocalDate maturityDate;
    private BigDecimal maturityAmount;
    private BigDecimal currentValue;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime closedAt;
    private Integer daysElapsed;
    private Integer totalDays;
}