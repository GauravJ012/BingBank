package com.bingbank.authService.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "pending_registrations")
public class PendingRegistration {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    private Integer age;
    
    private String gender;
    
    private String address;
    
    private String mobile;
    
    @Column(name = "two_factor_enabled")
    private Boolean twoFactorEnabled;
    
    @Column(name = "parent_customer_id")
    private Long parentCustomerId;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    private String accountNumber;
    
    @Column(name = "otp_code", nullable = false)
    private String otpCode;
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}