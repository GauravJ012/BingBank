package com.bingbank.cardsservice.repository;

import com.bingbank.cardsservice.model.DebitCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DebitCardRepository extends JpaRepository<DebitCard, Long> {
    
    Optional<DebitCard> findByCustomerId(Long customerId);
    
    Optional<DebitCard> findByAccountNumber(String accountNumber);
    
    Optional<DebitCard> findByCardNumber(String cardNumber);
}