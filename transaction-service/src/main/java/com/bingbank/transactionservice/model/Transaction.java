package com.bingbank.transactionservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transactions")
public class Transaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "transaction_type", nullable = false, length = 10)
    private String transactionType; // DEBIT or CREDIT
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "source_account_number", nullable = false, length = 20)
    private String sourceAccountNumber;
    
    @Column(name = "target_account_number", length = 20)
    private String targetAccountNumber;
    
    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;
}