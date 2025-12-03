package com.bingbank.cardsservice.service;

import com.bingbank.cardsservice.dto.*;
import com.bingbank.cardsservice.model.CreditCard;
import com.bingbank.cardsservice.model.CreditCardTransaction;
import com.bingbank.cardsservice.repository.CreditCardRepository;
import com.bingbank.cardsservice.repository.CreditCardTransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CreditCardService {

    @Autowired
    private CreditCardRepository creditCardRepository;

    @Autowired
    private CreditCardTransactionRepository transactionRepository;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Get credit card by customer ID
     */
    public CreditCardDTO getCreditCardByCustomerId(Long customerId) {
        System.out.println("CreditCardService: Fetching credit card for customer: " + customerId);
        
        CreditCard card = creditCardRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new RuntimeException("Credit card not found for customer"));
        
        return mapToDTO(card);
    }

    /**
     * Get all transactions for a credit card
     */
    public List<CreditCardTransactionDTO> getAllTransactions(Long cardId) {
        System.out.println("CreditCardService: Fetching all transactions for card: " + cardId);
        
        List<CreditCardTransaction> transactions = 
                transactionRepository.findByCardIdOrderByTransactionDateDescTransactionTimeDesc(cardId);
        
        return transactions.stream()
                .map(this::mapTransactionToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get filtered transactions
     */
    public List<CreditCardTransactionDTO> getFilteredTransactions(TransactionFilterRequest request) {
        System.out.println("CreditCardService: Fetching filtered transactions for card: " + request.getCardId());
        
        List<CreditCardTransaction> transactions = transactionRepository.findFilteredTransactions(
                request.getCardId(),
                request.getStartDate(),
                request.getEndDate(),
                request.getMinAmount(),
                request.getMaxAmount(),
                request.getCategory(),
                request.getMerchantName(),
                request.getTransactionType(),
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
                .map(this::mapTransactionToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get transactions for a specific month
     */
    public List<CreditCardTransactionDTO> getMonthlyTransactions(Long cardId, int year, int month) {
        System.out.println("CreditCardService: Fetching transactions for " + year + "-" + month);
        
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(1).minusDays(1);
        
        List<CreditCardTransaction> transactions = 
                transactionRepository.findByCardIdAndTransactionDateBetweenOrderByTransactionDateDescTransactionTimeDesc(
                        cardId, startDate, endDate);
        
        return transactions.stream()
                .map(this::mapTransactionToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Pay credit card bill
     */
    @Transactional
    public CreditCardDTO payBill(PayBillRequest request, String authHeader) {
        System.out.println("CreditCardService: Processing bill payment for card: " + request.getCardId());
        
        CreditCard card = creditCardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Credit card not found"));
        
        // Verify ownership
        if (!card.getCustomerId().equals(request.getCustomerId())) {
            throw new RuntimeException("Unauthorized to pay bill for this card");
        }
        
        // Verify account ownership
        if (!card.getAccountNumber().equals(request.getAccountNumber())) {
            throw new RuntimeException("Account number mismatch");
        }
        
        // Check if payment amount is valid
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Payment amount must be greater than zero");
        }
        
        if (request.getAmount().compareTo(card.getOutstandingBalance()) > 0) {
            throw new RuntimeException("Payment amount cannot exceed outstanding balance");
        }
        
        // Verify sufficient balance in account
        if (!verifyAccountBalance(request.getAccountNumber(), request.getAmount(), authHeader)) {
            throw new RuntimeException("Insufficient balance in account");
        }
        
        // Debit from account
        debitFromAccount(request.getAccountNumber(), request.getAmount(), 
                "Credit Card Payment - Card ending " + card.getCardNumber().substring(12), authHeader);
        
        // Update credit card balances
        card.setOutstandingBalance(card.getOutstandingBalance().subtract(request.getAmount()));
        card.setAvailableCredit(card.getAvailableCredit().add(request.getAmount()));
        
        CreditCard updatedCard = creditCardRepository.save(card);
        
        // Create payment transaction
        CreditCardTransaction payment = new CreditCardTransaction();
        payment.setCardId(card.getCardId());
        payment.setCardNumber(card.getCardNumber());
        payment.setTransactionDate(LocalDate.now());
        payment.setTransactionTime(java.time.LocalTime.now());
        payment.setMerchantName("Payment Received");
        payment.setCategory("Payment");
        payment.setAmount(request.getAmount());
        payment.setTransactionType("PAYMENT");
        payment.setStatus("COMPLETED");
        transactionRepository.save(payment);
        
        // Create account transaction
        createAccountTransaction(request.getAccountNumber(), request.getAmount(), "DEBIT",
                "Credit Card Payment - Card ending " + card.getCardNumber().substring(12), authHeader);
        
        System.out.println("CreditCardService: Bill payment completed successfully");
        
        return mapToDTO(updatedCard);
    }

    /**
     * Deactivate credit card
     */
    @Transactional
    public CreditCardDTO deactivateCard(DeactivateCardRequest request, String authHeader) {
        System.out.println("CreditCardService: Deactivating card: " + request.getCardId());
        
        CreditCard card = creditCardRepository.findById(request.getCardId())
                .orElseThrow(() -> new RuntimeException("Credit card not found"));
        
        // Verify ownership
        if (!card.getCustomerId().equals(request.getCustomerId())) {
            throw new RuntimeException("Unauthorized to deactivate this card");
        }
        
        // Check if already inactive
        if ("INACTIVE".equals(card.getCardStatus())) {
            throw new RuntimeException("Card is already inactive");
        }
        
        // Check if there's outstanding balance
        if (card.getOutstandingBalance().compareTo(BigDecimal.ZERO) > 0) {
            throw new RuntimeException("Cannot deactivate card with outstanding balance");
        }
        
        // Deactivate card
        card.setCardStatus("INACTIVE");
        
        CreditCard updatedCard = creditCardRepository.save(card);
        System.out.println("CreditCardService: Card deactivated successfully");
        
        return mapToDTO(updatedCard);
    }

    /**
     * Verify account balance
     */
    private boolean verifyAccountBalance(String accountNumber, BigDecimal amount, String authHeader) {
        try {
            String url = "http://localhost:8082/api/accounts/" + accountNumber;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object balanceObj = response.getBody().get("balance");
                BigDecimal balance = new BigDecimal(balanceObj.toString());
                return balance.compareTo(amount) >= 0;
            }
        } catch (Exception e) {
            System.err.println("Error verifying account balance: " + e.getMessage());
        }
        return false;
    }

    /**
     * Debit from account
     */
    private void debitFromAccount(String accountNumber, BigDecimal amount, String description, String authHeader) {
        try {
            String url = "http://localhost:8082/api/accounts/" + accountNumber + "/debit";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("amount", amount);
            request.put("description", description);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            System.out.println("Debited $" + amount + " from account: " + accountNumber);
        } catch (Exception e) {
            System.err.println("Error debiting from account: " + e.getMessage());
            throw new RuntimeException("Failed to debit from account");
        }
    }

    /**
     * Create account transaction
     */
    private void createAccountTransaction(String accountNumber, BigDecimal amount, String type,
                                          String description, String authHeader) {
        try {
            String url = "http://localhost:8083/api/transactions/create";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("accountNumber", accountNumber);
            request.put("amount", amount);
            request.put("transactionType", type);
            request.put("sourceAccountNumber", accountNumber);
            request.put("targetAccountNumber", "CREDIT_CARD_PAYMENT");
            request.put("description", description);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            System.out.println("Created " + type + " transaction for account: " + accountNumber);
        } catch (Exception e) {
            System.err.println("Error creating transaction: " + e.getMessage());
        }
    }

    /**
     * Map entity to DTO
     */
    private CreditCardDTO mapToDTO(CreditCard card) {
        CreditCardDTO dto = new CreditCardDTO();
        dto.setCardId(card.getCardId());
        dto.setCustomerId(card.getCustomerId());
        dto.setAccountNumber(card.getAccountNumber());
        dto.setCardNumber(card.getCardNumber());
        dto.setCardholderName(card.getCardholderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCvv(card.getCvv());
        dto.setCreditLimit(card.getCreditLimit());
        dto.setAvailableCredit(card.getAvailableCredit());
        dto.setOutstandingBalance(card.getOutstandingBalance());
        dto.setUsedCredit(card.getCreditLimit().subtract(card.getAvailableCredit()));
        dto.setPaymentDueDate(card.getPaymentDueDate());
        dto.setCardStatus(card.getCardStatus());
        
        // Format expiry date as MM/YY
        if (card.getExpiryDate() != null) {
            dto.setExpiryMonth(card.getExpiryDate().format(DateTimeFormatter.ofPattern("MM")));
            dto.setExpiryYear(card.getExpiryDate().format(DateTimeFormatter.ofPattern("yy")));
        }
        
        // Calculate days until due
        if (card.getPaymentDueDate() != null) {
            long days = ChronoUnit.DAYS.between(LocalDate.now(), card.getPaymentDueDate());
            dto.setDaysUntilDue((int) days);
        }
        
        return dto;
    }

    /**
     * Map transaction entity to DTO
     */
    private CreditCardTransactionDTO mapTransactionToDTO(CreditCardTransaction transaction) {
        CreditCardTransactionDTO dto = new CreditCardTransactionDTO();
        dto.setTransactionId(transaction.getTransactionId());
        dto.setCardId(transaction.getCardId());
        dto.setCardNumber(transaction.getCardNumber());
        dto.setTransactionDate(transaction.getTransactionDate());
        dto.setTransactionTime(transaction.getTransactionTime());
        dto.setMerchantName(transaction.getMerchantName());
        dto.setCategory(transaction.getCategory());
        dto.setAmount(transaction.getAmount());
        dto.setTransactionType(transaction.getTransactionType());
        dto.setStatus(transaction.getStatus());
        return dto;
    }
}