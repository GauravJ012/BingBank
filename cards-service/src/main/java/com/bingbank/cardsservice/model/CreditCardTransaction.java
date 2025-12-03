package com.bingbank.cardsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "credit_card_transactions")
public class CreditCardTransaction {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transaction_id")
    private Long transactionId;
    
    @Column(name = "card_id", nullable = false)
    private Long cardId;
    
    @Column(name = "card_number", nullable = false, length = 16)
    private String cardNumber;
    
    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
    
    @Column(name = "transaction_time", nullable = false)
    private LocalTime transactionTime;
    
    @Column(name = "merchant_name", nullable = false)
    private String merchantName;
    
    @Column(length = 50)
    private String category;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // PURCHASE, PAYMENT, REFUND
    
    @Column(nullable = false, length = 20)
    private String status; // COMPLETED, PENDING, FAILED
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}