package com.bingbank.authService.repository;

import com.bingbank.authService.model.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {
    
    Optional<PendingRegistration> findByEmail(String email);
    
    Optional<PendingRegistration> findByEmailAndOtpCodeAndExpiresAtGreaterThan(
            String email, String otpCode, LocalDateTime now);
    
    void deleteByEmail(String email);
}