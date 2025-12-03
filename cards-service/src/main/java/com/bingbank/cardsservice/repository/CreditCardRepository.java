package com.bingbank.cardsservice.repository;

import com.bingbank.cardsservice.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {
    
    Optional<CreditCard> findByCustomerId(Long customerId);
    
    Optional<CreditCard> findByAccountNumber(String accountNumber);
    
    Optional<CreditCard> findByCardNumber(String cardNumber);
}