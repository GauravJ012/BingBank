package com.bingbank.accountservice.dto;

import lombok.Data;

@Data
public class BranchDTO {
    private Integer branchId;
    private String branchName;
    private String branchCode;
    private String city;
    private String state;
    private String zipcode;
}