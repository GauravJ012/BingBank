package com.bingbank.accountservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchDTO {
    private Long branchId;
    private String branchName;
    private String branchCode;
    private String city;
    private String state;
    private String zipcode;
}