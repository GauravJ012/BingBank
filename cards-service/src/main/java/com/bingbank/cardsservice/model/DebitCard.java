package com.bingbank.cardsservice.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "debit_cards")
public class DebitCard {
    
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
    
    @Column(name = "atm_pin", nullable = false)
    private String atmPin; // Encrypted
    
    @Column(name = "card_status", nullable = false, length = 20)
    private String cardStatus; // ACTIVE, INACTIVE, BLOCKED
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}