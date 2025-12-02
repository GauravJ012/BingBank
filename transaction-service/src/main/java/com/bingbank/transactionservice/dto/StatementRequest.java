package com.bingbank.transactionservice.dto;

import lombok.Data;
import java.util.List;

@Data
public class StatementRequest {
    private Long customerId;
    private String accountNumber;
    private List<Long> transactionIds;
}