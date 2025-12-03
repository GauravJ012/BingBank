package com.bingbank.fixeddepositservice.dto;

import lombok.Data;

@Data
public class CloseFDRequest {
    private Long fdId;
    private Long customerId;
    private String accountNumber;
}