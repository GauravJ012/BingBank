package com.bingbank.fixeddepositservice.repository;

import com.bingbank.fixeddepositservice.model.FixedDeposit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {
    
    List<FixedDeposit> findByCustomerIdAndStatus(Long customerId, String status);
    
    List<FixedDeposit> findByCustomerId(Long customerId);
    
    List<FixedDeposit> findByAccountNumberAndStatus(String accountNumber, String status);
}