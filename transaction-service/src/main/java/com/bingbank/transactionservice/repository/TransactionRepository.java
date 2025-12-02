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
    
    // Find all transactions for an account
    List<Transaction> findByAccountNumberOrderByTransactionDateDesc(String accountNumber);
    
    // Find latest N transactions for an account
    List<Transaction> findTop5ByAccountNumberOrderByTransactionDateDesc(String accountNumber);
    
    // Custom query for filtering
    @Query("SELECT t FROM Transaction t WHERE t.accountNumber = :accountNumber " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "AND (:otherAccount IS NULL OR t.sourceAccountNumber = :otherAccount OR t.targetAccountNumber = :otherAccount) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'transactionDate' AND :sortDirection = 'ASC' THEN t.transactionDate END ASC, " +
           "CASE WHEN :sortBy = 'transactionDate' AND :sortDirection = 'DESC' THEN t.transactionDate END DESC, " +
           "CASE WHEN :sortBy = 'amount' AND :sortDirection = 'ASC' THEN t.amount END ASC, " +
           "CASE WHEN :sortBy = 'amount' AND :sortDirection = 'DESC' THEN t.amount END DESC, " +
           "CASE WHEN :sortBy = 'transactionType' AND :sortDirection = 'ASC' THEN t.transactionType END ASC, " +
           "CASE WHEN :sortBy = 'transactionType' AND :sortDirection = 'DESC' THEN t.transactionType END DESC, " +
           "t.transactionDate DESC")
    List<Transaction> findFilteredTransactions(
            @Param("accountNumber") String accountNumber,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("transactionType") String transactionType,
            @Param("otherAccount") String otherAccount,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection
    );
    
    // Find specific transactions by IDs
    List<Transaction> findByTransactionIdIn(List<Long> transactionIds);
}