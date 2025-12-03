package com.bingbank.transactionservice.service;

import com.bingbank.transactionservice.dto.TransactionDTO;
import com.bingbank.transactionservice.dto.TransactionFilterRequest;
import com.bingbank.transactionservice.model.Transaction;
import com.bingbank.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Get latest 5 transactions for an account (for dashboard)
     * Sorted by transaction_id DESC
     */
    public List<TransactionDTO> getLatestTransactions(String accountNumber) {
        System.out.println("TransactionService: Fetching latest 5 transactions for account: " + accountNumber);
        List<Transaction> transactions = transactionRepository
                .findTop5ByAccountNumberOrderByTransactionIdDesc(accountNumber);
        
        // Take only first 5
        return transactions.stream()
                .limit(5)
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all transactions for an account
     * Sorted by transaction_id DESC
     */
    public List<TransactionDTO> getAllTransactions(String accountNumber) {
        System.out.println("TransactionService: Fetching all transactions for account: " + accountNumber);
        List<Transaction> transactions = transactionRepository
                .findByAccountNumberOrderByTransactionIdDesc(accountNumber);
        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered transactions
     * Sorted by transaction_id DESC
     */
    public List<TransactionDTO> getFilteredTransactions(TransactionFilterRequest request) {
        System.out.println("TransactionService: Fetching filtered transactions");
        
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                request.getAccountNumber(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinAmount(),
                request.getMaxAmount(),
                request.getTransactionType(),
                request.getOtherAccountNumber()
        );
        
        // Apply limit if specified
        if (request.getLimit() != null && request.getLimit() > 0) {
            transactions = transactions.stream()
                    .limit(request.getLimit())
                    .collect(Collectors.toList());
        }
        
        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Create a new transaction
     */
    public TransactionDTO createTransaction(String accountNumber, BigDecimal amount, String transactionType,
                                           String sourceAccountNumber, String targetAccountNumber) {
        System.out.println("TransactionService: Creating " + transactionType + " transaction for account: " + accountNumber);
        
        Transaction transaction = new Transaction();
        transaction.setAccountNumber(accountNumber);
        transaction.setAmount(amount);
        transaction.setTransactionType(transactionType);
        transaction.setTransactionDate(LocalDate.now());
        transaction.setSourceAccountNumber(sourceAccountNumber);
        transaction.setTargetAccountNumber(targetAccountNumber != null ? targetAccountNumber : "N/A");
        
        Transaction savedTransaction = transactionRepository.save(transaction);
        System.out.println("TransactionService: Transaction created with ID: " + savedTransaction.getTransactionId());
        
        return mapToDTO(savedTransaction);
    }

    /**
     * Get specific transactions by IDs (for PDF generation)
     */
    public List<TransactionDTO> getTransactionsByIds(List<Long> transactionIds) {
        List<Transaction> transactions = transactionRepository.findByTransactionIdIn(transactionIds);
        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Map Transaction entity to DTO
     */
    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setSourceAccountNumber(transaction.getSourceAccountNumber());
        dto.setTargetAccountNumber(transaction.getTargetAccountNumber());
        dto.setAccountNumber(transaction.getAccountNumber());
        return dto;
    }
}