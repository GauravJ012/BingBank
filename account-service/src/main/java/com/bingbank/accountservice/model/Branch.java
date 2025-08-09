package com.bingbank.accountservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "branches")
public class Branch {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "branch_id")
    private Long branchId;
    
    @Column(name = "branch_name", nullable = false)
    private String branchName;
    
    @Column(name = "branch_code", nullable = false, unique = true)
    private String branchCode;
    
    @Column(nullable = false)
    private String city;
    
    @Column(nullable = false)
    private String state;
    
    @Column(nullable = false)
    private String zipcode;
}