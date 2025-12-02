package com.bingbank.accountservice.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class AccountDTO {
    private String accountNumber;
    private Long customerId;
    private String accountType;
    private String routingNumber;
    private BigDecimal balance;
    private BranchDTO branch;
}