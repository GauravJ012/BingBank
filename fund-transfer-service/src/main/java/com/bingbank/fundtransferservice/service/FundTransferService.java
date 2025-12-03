package com.bingbank.fundtransferservice.service;

import com.bingbank.fundtransferservice.dto.TransferEvent;
import com.bingbank.fundtransferservice.dto.TransferRequest;
import com.bingbank.fundtransferservice.dto.TransferResponse;
import com.bingbank.fundtransferservice.model.FundTransfer;
import com.bingbank.fundtransferservice.repository.FundTransferRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FundTransferService {

    @Autowired
    private FundTransferRepository transferRepository;

    @Autowired
    private KafkaTemplate<String, TransferEvent> kafkaTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.kafka.topic.fund-transfer}")
    private String fundTransferTopic;

    /**
     * Initiate a fund transfer
     */
    @Transactional
    public TransferResponse initiateTransfer(TransferRequest request, String authHeader) {
        System.out.println("FundTransferService: Initiating transfer from " + 
                request.getSourceAccountNumber() + " to " + request.getTargetAccountNumber());
        
        // Validate accounts exist
        if (!validateAccountExists(request.getTargetAccountNumber(), authHeader)) {
            throw new RuntimeException("Target account does not exist");
        }
        
        // Validate source account belongs to customer
        if (!validateAccountOwnership(request.getSourceAccountNumber(), request.getCustomerId(), authHeader)) {
            throw new RuntimeException("You are not authorized to transfer from this account");
        }
        
        // Validate sufficient balance
        if (!validateSufficientBalance(request.getSourceAccountNumber(), request.getAmount(), authHeader)) {
            throw new RuntimeException("Insufficient balance in source account");
        }
        
        // Validate not transferring to same account
        if (request.getSourceAccountNumber().equals(request.getTargetAccountNumber())) {
            throw new RuntimeException("Cannot transfer to the same account");
        }
        
        // Create transfer record
        FundTransfer transfer = new FundTransfer();
        transfer.setCustomerId(request.getCustomerId());
        transfer.setSourceAccountNumber(request.getSourceAccountNumber());
        transfer.setTargetAccountNumber(request.getTargetAccountNumber());
        transfer.setAmount(request.getAmount());
        transfer.setTransferDate(LocalDateTime.now());
        transfer.setStatus("PENDING");
        transfer.setRemarks(request.getRemarks());
        
        FundTransfer savedTransfer = transferRepository.save(transfer);
        System.out.println("FundTransferService: Transfer record created with ID: " + savedTransfer.getTransferId());
        
        // Publish event to Kafka
        TransferEvent event = new TransferEvent(
                savedTransfer.getTransferId(),
                savedTransfer.getCustomerId(),
                savedTransfer.getSourceAccountNumber(),
                savedTransfer.getTargetAccountNumber(),
                savedTransfer.getAmount(),
                savedTransfer.getTransferDate(),
                savedTransfer.getRemarks()
        );
        
        kafkaTemplate.send(fundTransferTopic, event.getTransferId().toString(), event);
        System.out.println("FundTransferService: Transfer event published to Kafka");
        
        // Update status to PROCESSING
        savedTransfer.setStatus("PROCESSING");
        transferRepository.save(savedTransfer);
        
        return mapToResponse(savedTransfer, "Transfer initiated successfully and is being processed");
    }

    /**
     * Get transfer history for a customer
     */
    public List<TransferResponse> getTransferHistory(Long customerId) {
        System.out.println("FundTransferService: Fetching transfer history for customer: " + customerId);
        List<FundTransfer> transfers = transferRepository.findByCustomerIdOrderByTransferDateDesc(customerId);
        return transfers.stream()
                .map(t -> mapToResponse(t, null))
                .collect(Collectors.toList());
    }

    /**
     * Get transfer by ID
     */
    public TransferResponse getTransferById(Long transferId) {
        FundTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        return mapToResponse(transfer, null);
    }

    /**
     * Update transfer status
     */
    @Transactional
    public void updateTransferStatus(Long transferId, String status) {
        FundTransfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));
        transfer.setStatus(status);
        transferRepository.save(transfer);
        System.out.println("FundTransferService: Transfer " + transferId + " status updated to " + status);
    }

    /**
     * Validate account exists
     */
    private boolean validateAccountExists(String accountNumber, String authHeader) {
        try {
            String url = "http://localhost:8082/api/accounts/exists/" + accountNumber;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (Boolean) response.getBody().get("exists");
            }
        } catch (Exception e) {
            System.err.println("Error validating account existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Validate account ownership
     */
    private boolean validateAccountOwnership(String accountNumber, Long customerId, String authHeader) {
        try {
            String url = "http://localhost:8082/api/accounts/" + accountNumber;
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", authHeader);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Object customerIdObj = response.getBody().get("customerId");
                Long accountCustomerId = customerIdObj instanceof Integer ? 
                        ((Integer) customerIdObj).longValue() : (Long) customerIdObj;
                return accountCustomerId.equals(customerId);
            }
        } catch (Exception e) {
            System.err.println("Error validating account ownership: " + e.getMessage());
        }
        return false;
    }

    /**
     * Validate sufficient balance
     */
    private boolean validateSufficientBalance(String accountNumber, BigDecimal amount, String authHeader) {
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
            System.err.println("Error validating balance: " + e.getMessage());
        }
        return false;
    }

    /**
     * Map entity to response DTO
     */
    private TransferResponse mapToResponse(FundTransfer transfer, String message) {
        TransferResponse response = new TransferResponse();
        response.setTransferId(transfer.getTransferId());
        response.setCustomerId(transfer.getCustomerId());
        response.setSourceAccountNumber(transfer.getSourceAccountNumber());
        response.setTargetAccountNumber(transfer.getTargetAccountNumber());
        response.setAmount(transfer.getAmount());
        response.setTransferDate(transfer.getTransferDate());
        response.setStatus(transfer.getStatus());
        response.setRemarks(transfer.getRemarks());
        response.setMessage(message);
        return response;
    }
}