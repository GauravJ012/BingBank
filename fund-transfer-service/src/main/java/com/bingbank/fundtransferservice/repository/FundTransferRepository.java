package com.bingbank.fundtransferservice.repository;

import com.bingbank.fundtransferservice.model.FundTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FundTransferRepository extends JpaRepository<FundTransfer, Long> {
    
    List<FundTransfer> findByCustomerIdOrderByTransferDateDesc(Long customerId);
    
    List<FundTransfer> findBySourceAccountNumberOrderByTransferDateDesc(String sourceAccountNumber);
    
    List<FundTransfer> findByCustomerIdAndStatusOrderByTransferDateDesc(Long customerId, String status);
}