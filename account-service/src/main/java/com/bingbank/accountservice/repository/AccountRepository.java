package com.bingbank.accountservice.repository;

import com.bingbank.accountservice.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {
    
    List<Account> findByCustomerId(Long customerId);
    
    boolean existsByAccountNumber(String accountNumber);
}