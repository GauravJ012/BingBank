package com.bingbank.fundtransferservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fund_transfers")
public class FundTransfer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "source_account_number", nullable = false, length = 20)
    private String sourceAccountNumber;
    
    @Column(name = "target_account_number", nullable = false, length = 20)
    private String targetAccountNumber;
    
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "transfer_date", nullable = false)
    private LocalDateTime transferDate;
    
    @Column(nullable = false, length = 20)
    private String status; // PENDING, PROCESSING, COMPLETED, FAILED
    
    @Column(length = 255)
    private String remarks;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}