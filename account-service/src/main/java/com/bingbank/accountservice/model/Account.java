package com.bingbank.accountservice.model;

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
@Table(name = "accounts")
public class Account {
    
    @Id
    @Column(name = "account_number")
    private String accountNumber;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "account_type", nullable = false)
    private String accountType;
    
    @Column(name = "routing_number", nullable = false)
    private String routingNumber;
    
    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "branch_id", nullable = false)
    private Branch branch;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}