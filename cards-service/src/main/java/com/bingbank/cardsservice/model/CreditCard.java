package com.bingbank.cardsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_cards")
public class CreditCard {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "card_id")
    private Long cardId;
    
    @Column(name = "customer_id", nullable = false)
    private Long customerId;
    
    @Column(name = "account_number", nullable = false, length = 20)
    private String accountNumber;
    
    @Column(name = "card_number", nullable = false, unique = true, length = 16)
    private String cardNumber;
    
    @Column(name = "cardholder_name", nullable = false, length = 100)
    private String cardholderName;
    
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;
    
    @Column(nullable = false, length = 3)
    private String cvv;
    
    @Column(name = "credit_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal creditLimit;
    
    @Column(name = "available_credit", nullable = false, precision = 15, scale = 2)
    private BigDecimal availableCredit;
    
    @Column(name = "outstanding_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal outstandingBalance;
    
    @Column(name = "billing_cycle_day", nullable = false)
    private Integer billingCycleDay;
    
    @Column(name = "payment_due_date")
    private LocalDate paymentDueDate;
    
    @Column(name = "card_status", nullable = false, length = 20)
    private String cardStatus; // ACTIVE, INACTIVE, BLOCKED
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}