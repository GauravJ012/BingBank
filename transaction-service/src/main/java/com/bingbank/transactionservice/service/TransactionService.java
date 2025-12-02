package com.bingbank.transactionservice.service;

import com.bingbank.transactionservice.dto.TransactionDTO;
import com.bingbank.transactionservice.dto.TransactionFilterRequest;
import com.bingbank.transactionservice.model.Transaction;
import com.bingbank.transactionservice.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    /**
     * Get latest 5 transactions for an account (for dashboard)
     */
    public List<TransactionDTO> getLatestTransactions(String accountNumber) {
        System.out.println("TransactionService: Fetching latest 5 transactions for account: " + accountNumber);
        List<Transaction> transactions = transactionRepository
                .findTop5ByAccountNumberOrderByTransactionDateDesc(accountNumber);
        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get all transactions for an account
     */
    public List<TransactionDTO> getAllTransactions(String accountNumber) {
        System.out.println("TransactionService: Fetching all transactions for account: " + accountNumber);
        List<Transaction> transactions = transactionRepository
                .findByAccountNumberOrderByTransactionDateDesc(accountNumber);
        return transactions.stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered and sorted transactions
     */
    public List<TransactionDTO> getFilteredTransactions(TransactionFilterRequest request) {
        System.out.println("TransactionService: Fetching filtered transactions");
        
        // Set defaults
        if (request.getSortBy() == null) {
            request.setSortBy("transactionDate");
        }
        if (request.getSortDirection() == null) {
            request.setSortDirection("DESC");
        }
        
        List<Transaction> transactions = transactionRepository.findFilteredTransactions(
                request.getAccountNumber(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinAmount(),
                request.getMaxAmount(),
                request.getTransactionType(),
                request.getOtherAccountNumber(),
                request.getSortBy(),
                request.getSortDirection()
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