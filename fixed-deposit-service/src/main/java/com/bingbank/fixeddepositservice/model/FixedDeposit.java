package com.bingbank.fixeddepositservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "fixed_deposits")
public class FixedDeposit {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fd_id")
    private Long fdId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;
    
    @Column(name = "principal_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal principalAmount;
    
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 4)
    private BigDecimal interestRate;
    
    @Column(name = "tenure_years", nullable = false)
    private Integer tenureYears;
    
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    @Column(name = "maturity_date", nullable = false)
    private LocalDate maturityDate;
    
    @Column(name = "maturity_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal maturityAmount;
    
    @Column(name = "current_value", nullable = false, precision = 15, scale = 2)
    private BigDecimal currentValue;
    
    @Column(nullable = false, length = 20)
    private String status; // ACTIVE, CLOSED
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "closed_at")
    private LocalDateTime closedAt;
}