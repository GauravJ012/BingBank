package com.bingbank.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountDTO {
    private String accountNumber;
    private Long customerId;
    private String accountType;
    private String routingNumber;
    private BigDecimal balance;
    private BranchDTO branch;
}