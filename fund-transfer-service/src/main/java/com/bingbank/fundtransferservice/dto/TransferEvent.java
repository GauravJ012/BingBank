package com.bingbank.fundtransferservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferEvent {
    private Long transferId;
    private Long customerId;
    private String sourceAccountNumber;
    private String targetAccountNumber;
    private BigDecimal amount;
    private LocalDateTime transferDate;
    private String remarks;
}