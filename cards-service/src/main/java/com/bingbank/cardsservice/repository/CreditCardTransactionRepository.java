package com.bingbank.cardsservice.repository;

import com.bingbank.cardsservice.model.CreditCardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CreditCardTransactionRepository extends JpaRepository<CreditCardTransaction, Long> {
    
	@Query("SELECT t FROM CreditCardTransaction t WHERE t.cardId = :cardId ORDER BY t.transactionId DESC")
	List<CreditCardTransaction> findByCardIdOrderByTransactionDateDescTransactionTimeDesc(@Param("cardId") Long cardId);
    
	@Query("SELECT t FROM CreditCardTransaction t WHERE t.cardId = :cardId " +
		       "AND t.transactionDate >= :startDate AND t.transactionDate <= :endDate " +
		       "ORDER BY t.transactionId DESC")
		List<CreditCardTransaction> findByCardIdAndTransactionDateBetweenOrderByTransactionDateDescTransactionTimeDesc(
		        @Param("cardId") Long cardId, 
		        @Param("startDate") LocalDate startDate, 
		        @Param("endDate") LocalDate endDate);
    
    @Query("SELECT t FROM CreditCardTransaction t WHERE t.cardId = :cardId " +
           "AND (:startDate IS NULL OR t.transactionDate >= :startDate) " +
           "AND (:endDate IS NULL OR t.transactionDate <= :endDate) " +
           "AND (:minAmount IS NULL OR t.amount >= :minAmount) " +
           "AND (:maxAmount IS NULL OR t.amount <= :maxAmount) " +
           "AND (:category IS NULL OR t.category = :category) " +
           "AND (:merchantName IS NULL OR LOWER(t.merchantName) LIKE LOWER(CONCAT('%', :merchantName, '%'))) " +
           "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
           "ORDER BY " +
           "CASE WHEN :sortBy = 'amount' AND :sortDirection = 'asc' THEN t.amount END ASC, " +
           "CASE WHEN :sortBy = 'amount' AND :sortDirection = 'desc' THEN t.amount END DESC, " +
           "CASE WHEN :sortBy = 'merchant' AND :sortDirection = 'asc' THEN t.merchantName END ASC, " +
           "CASE WHEN :sortBy = 'merchant' AND :sortDirection = 'desc' THEN t.merchantName END DESC, " +
           "CASE WHEN :sortBy = 'date' OR :sortBy IS NULL THEN t.transactionDate END DESC, " +
           "t.transactionTime DESC")
    List<CreditCardTransaction> findFilteredTransactions(
            @Param("cardId") Long cardId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("minAmount") BigDecimal minAmount,
            @Param("maxAmount") BigDecimal maxAmount,
            @Param("category") String category,
            @Param("merchantName") String merchantName,
            @Param("transactionType") String transactionType,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection
    );
}