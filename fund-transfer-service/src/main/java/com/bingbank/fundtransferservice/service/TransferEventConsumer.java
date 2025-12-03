package com.bingbank.fundtransferservice.service;

import com.bingbank.fundtransferservice.dto.TransferEvent;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class TransferEventConsumer {

    @Autowired
    private FundTransferService fundTransferService;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    /**
     * Consume transfer events from Kafka and process them
     */
    @KafkaListener(topics = "${app.kafka.topic.fund-transfer}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeTransferEvent(TransferEvent event) {
        System.out.println("============================================");
        System.out.println("Consumer: Received transfer event: " + event.getTransferId());
        System.out.println("Consumer: From " + event.getSourceAccountNumber() + " to " + event.getTargetAccountNumber());
        System.out.println("Consumer: Amount: $" + event.getAmount());
        System.out.println("============================================");

        try {
            // Process the transfer
            processTransfer(event);
            
            // Update status to COMPLETED
            fundTransferService.updateTransferStatus(event.getTransferId(), "COMPLETED");
            System.out.println("Consumer: Transfer " + event.getTransferId() + " completed successfully");
            
        } catch (Exception e) {
            System.err.println("Consumer: Error processing transfer " + event.getTransferId() + ": " + e.getMessage());
            e.printStackTrace();
            
            // Update status to FAILED
            try {
                fundTransferService.updateTransferStatus(event.getTransferId(), "FAILED");
            } catch (Exception ex) {
                System.err.println("Consumer: Error updating status to FAILED: " + ex.getMessage());
            }
        }
    }

    /**
     * Generate a service-to-service JWT token
     */
    private String generateServiceToken() {
        try {
            Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + 3600000); // 1 hour validity
            
            String token = Jwts.builder()
                    .setSubject("fund-transfer-service")
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key)
                    .compact();
            
            System.out.println("Consumer: Generated service token");
            return "Bearer " + token;
        } catch (Exception e) {
            System.err.println("Consumer: Error generating service token: " + e.getMessage());
            throw new RuntimeException("Failed to generate service token");
        }
    }

    /**
     * Process the actual transfer
     */
    private void processTransfer(TransferEvent event) {
        System.out.println("Consumer: Processing transfer " + event.getTransferId());
        
        // Generate service token for inter-service communication
        String authToken = generateServiceToken();
        
        try {
            // Step 1: Debit from source account
            debitFromAccount(event.getSourceAccountNumber(), event.getAmount(), 
                    "Transfer to " + event.getTargetAccountNumber() + " - Transfer#" + event.getTransferId(),
                    authToken);
            
            // Step 2: Credit to target account
            creditToAccount(event.getTargetAccountNumber(), event.getAmount(), 
                    "Transfer from " + event.getSourceAccountNumber() + " - Transfer#" + event.getTransferId(),
                    authToken);
            
            // Step 3: Create debit transaction for source account
            createTransaction(event.getSourceAccountNumber(), event.getAmount(), "DEBIT",
                    event.getSourceAccountNumber(), event.getTargetAccountNumber(),
                    "Fund Transfer - Transfer#" + event.getTransferId(), authToken);
            
            // Step 4: Create credit transaction for target account
            createTransaction(event.getTargetAccountNumber(), event.getAmount(), "CREDIT",
                    event.getSourceAccountNumber(), event.getTargetAccountNumber(),
                    "Fund Transfer - Transfer#" + event.getTransferId(), authToken);
            
            System.out.println("Consumer: Transfer processing completed successfully");
            
        } catch (Exception e) {
            System.err.println("Consumer: Error in transfer processing: " + e.getMessage());
            throw new RuntimeException("Transfer processing failed: " + e.getMessage());
        }
    }

    /**
     * Debit from account
     */
    private void debitFromAccount(String accountNumber, Object amount, String description, String authToken) {
        try {
            String url = "http://localhost:8082/api/accounts/" + accountNumber + "/debit";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("amount", amount);
            request.put("description", description);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to debit from account");
            }
            
            System.out.println("Consumer: Debited $" + amount + " from account " + accountNumber);
        } catch (Exception e) {
            System.err.println("Consumer: Error debiting from account: " + e.getMessage());
            throw new RuntimeException("Debit failed: " + e.getMessage());
        }
    }

    /**
     * Credit to account
     */
    private void creditToAccount(String accountNumber, Object amount, String description, String authToken) {
        try {
            String url = "http://localhost:8082/api/accounts/" + accountNumber + "/credit";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("amount", amount);
            request.put("description", description);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to credit to account");
            }
            
            System.out.println("Consumer: Credited $" + amount + " to account " + accountNumber);
        } catch (Exception e) {
            System.err.println("Consumer: Error crediting to account: " + e.getMessage());
            throw new RuntimeException("Credit failed: " + e.getMessage());
        }
    }

    /**
     * Create transaction record
     */
    private void createTransaction(String accountNumber, Object amount, String type,
                                   String sourceAccount, String targetAccount, 
                                   String description, String authToken) {
        try {
            String url = "http://localhost:8083/api/transactions/create";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authToken);
            headers.set("Content-Type", "application/json");
            
            Map<String, Object> request = new HashMap<>();
            request.put("accountNumber", accountNumber);
            request.put("amount", amount);
            request.put("transactionType", type);
            request.put("sourceAccountNumber", sourceAccount);
            request.put("targetAccountNumber", targetAccount);
            request.put("description", description);
            
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(request, headers);
            restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);
            
            System.out.println("Consumer: Created " + type + " transaction for account " + accountNumber);
        } catch (Exception e) {
            System.err.println("Consumer: Error creating transaction: " + e.getMessage());
            // Don't throw exception - transaction recording is secondary
        }
    }
}