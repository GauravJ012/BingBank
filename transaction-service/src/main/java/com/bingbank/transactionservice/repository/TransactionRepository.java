package com.bingbank.transactionservice.repository;

import com.bingbank.transactionservice.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    // Find all transactions for an account (sorted by transaction_id DESC)
    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber ORDER BY t.transactionId DESC")
    List<Transaction> findByAccountNumberOrderByTransactionIdDesc(@Param("accountNumber") String accountNumber);
    
    // Find latest 5 transactions for an account (sorted by transaction_id DESC)
    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber ORDER BY t.transactionId DESC")
    List<Transaction> findTop5ByAccountNumberOrderByTransactionIdDesc(@Param("accountNumber") String accountNumber);
    
    // Custom query for filtering (sorted by transaction_id DESC)
    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "AND (:otherAccount IS NULL OR t.sourceAccountNumber = :otherAccount OR t.targetAccountNumber = :otherAccount) " +
           "ORDER BY t.transactionId DESC")
    List<Transaction> findFilteredTransactions(
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("transactionType") String transactionType,
            @Param("otherAccount") String otherAccount
    );
    
    // Find specific transactions by IDs
    List<Transaction> findByTransactionIdIn(List<Long> transactionIds);
}