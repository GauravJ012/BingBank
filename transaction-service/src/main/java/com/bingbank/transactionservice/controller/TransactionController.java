package com.bingbank.transactionservice.controller;

import com.bingbank.transactionservice.dto.StatementRequest;
import com.bingbank.transactionservice.dto.TransactionDTO;
import com.bingbank.transactionservice.dto.TransactionFilterRequest;
import com.bingbank.transactionservice.service.PdfService;
import com.bingbank.transactionservice.service.TransactionService;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private PdfService pdfService;

    @Autowired
    private RestTemplate restTemplate;

    /**
     * Get latest 5 transactions for dashboard
     */
    @GetMapping("/latest/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getLatestTransactions(@PathVariable String accountNumber) {
        System.out.println("TransactionController: Fetching latest transactions for account: " + accountNumber);
        List<TransactionDTO> transactions = transactionService.getLatestTransactions(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get all transactions for an account
     */
    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<List<TransactionDTO>> getAllTransactions(@PathVariable String accountNumber) {
        System.out.println("TransactionController: Fetching all transactions for account: " + accountNumber);
        List<TransactionDTO> transactions = transactionService.getAllTransactions(accountNumber);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Get filtered and sorted transactions
     */
    @PostMapping("/filter")
    public ResponseEntity<List<TransactionDTO>> getFilteredTransactions(
            @RequestBody TransactionFilterRequest request) {
        System.out.println("TransactionController: Filtering transactions for account: " + request.getAccountNumber());
        List<TransactionDTO> transactions = transactionService.getFilteredTransactions(request);
        return ResponseEntity.ok(transactions);
    }

    /**
     * Generate and download bank statement PDF
     */
    @PostMapping("/statement/pdf")
    public ResponseEntity<byte[]> generateStatement(
            @RequestBody StatementRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("TransactionController: Generating statement for account: " + request.getAccountNumber());
            System.out.println("TransactionController: Customer ID from request: " + request.getCustomerId());
            
            // Get transactions
            List<TransactionDTO> transactions;
            if (request.getTransactionIds() != null && !request.getTransactionIds().isEmpty()) {
                transactions = transactionService.getTransactionsByIds(request.getTransactionIds());
            } else {
                transactions = transactionService.getAllTransactions(request.getAccountNumber());
            }
            
            // Fetch customer info from auth-service
            Map<String, Object> customerInfo = null;
            try {
                String authServiceUrl = "http://localhost:8081/api/auth/customer/" + request.getCustomerId();
                System.out.println("Fetching customer info from: " + authServiceUrl);
                
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Authorization", authHeader);
                org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
                
                ResponseEntity<Map> customerResponse = restTemplate.exchange(
                    authServiceUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
                );
                
                if (customerResponse.getStatusCode() == HttpStatus.OK && customerResponse.getBody() != null) {
                    Map<String, Object> fetchedData = customerResponse.getBody();
                    customerInfo = new java.util.HashMap<>();
                    
                    // Handle both 'id' and 'customerId' field names
                    Object customerIdObj = fetchedData.get("customerId");
                    if (customerIdObj == null) {
                        customerIdObj = fetchedData.get("id");
                    }
                    if (customerIdObj == null) {
                        customerIdObj = request.getCustomerId(); // Fallback to request
                    }
                    
                    customerInfo.put("customerId", customerIdObj);
                    customerInfo.put("firstName", fetchedData.getOrDefault("firstName", "Not Available"));
                    customerInfo.put("lastName", fetchedData.getOrDefault("lastName", "Not Available"));
                    customerInfo.put("email", fetchedData.getOrDefault("email", "Not Available"));
                    customerInfo.put("mobile", fetchedData.getOrDefault("mobile", "Not Available"));
                    customerInfo.put("address", fetchedData.getOrDefault("address", "Not Available"));
                    
                    System.out.println("Customer info fetched successfully: " + customerInfo);
                }
            } catch (Exception e) {
                System.err.println("Error fetching customer info: " + e.getMessage());
                e.printStackTrace();
            }
            
            // If we couldn't fetch customer info, use defaults with request customer ID
            if (customerInfo == null) {
                customerInfo = new java.util.HashMap<>();
                customerInfo.put("customerId", request.getCustomerId());
                customerInfo.put("firstName", "Customer");
                customerInfo.put("lastName", "Name");
                customerInfo.put("email", "Not Available");
                customerInfo.put("mobile", "Not Available");
                customerInfo.put("address", "Not Available");
            }
            
            // Fetch account info from account-service
            Map<String, Object> accountInfo = null;
            try {
                String accountServiceUrl = "http://localhost:8082/api/accounts/" + request.getAccountNumber();
                System.out.println("Fetching account info from: " + accountServiceUrl);
                
                org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
                headers.set("Authorization", authHeader);
                org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
                
                ResponseEntity<Map> accountResponse = restTemplate.exchange(
                    accountServiceUrl,
                    org.springframework.http.HttpMethod.GET,
                    entity,
                    Map.class
                );
                
                if (accountResponse.getStatusCode() == HttpStatus.OK && accountResponse.getBody() != null) {
                    Map<String, Object> accountData = accountResponse.getBody();
                    accountInfo = new java.util.HashMap<>();
                    accountInfo.put("accountNumber", accountData.get("accountNumber"));
                    accountInfo.put("accountType", accountData.get("accountType"));
                    accountInfo.put("balance", accountData.get("balance"));
                    
                    // Extract branch info if available
                    if (accountData.get("branch") != null && accountData.get("branch") instanceof Map) {
                        Map<String, Object> branch = (Map<String, Object>) accountData.get("branch");
                        accountInfo.put("branchCode", branch.get("branchCode"));
                    } else {
                        accountInfo.put("branchCode", "N/A");
                    }
                    
                    System.out.println("Account info fetched successfully: " + accountInfo);
                }
            } catch (Exception e) {
                System.err.println("Error fetching account info: " + e.getMessage());
                e.printStackTrace();
            }
            
            // If we couldn't fetch account info, use defaults
            if (accountInfo == null) {
                accountInfo = new java.util.HashMap<>();
                accountInfo.put("accountNumber", request.getAccountNumber());
                accountInfo.put("accountType", "Not Available");
                accountInfo.put("branchCode", "Not Available");
                accountInfo.put("balance", "0.00");
            }
            
            // Generate PDF
            byte[] pdfBytes = pdfService.generateBankStatement(customerInfo, accountInfo, transactions);
            
            // Set headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "bank_statement_" + request.getAccountNumber() + "_" + 
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (DocumentException e) {
            System.err.println("Error generating PDF: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    
    /**
     * Create a new transaction (for FD operations)
     */
    @PostMapping("/create")
    public ResponseEntity<?> createTransaction(@RequestBody Map<String, Object> request) {
        try {
            System.out.println("TransactionController: Create transaction request received");
            
            String accountNumber = request.get("accountNumber").toString();
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String transactionType = request.get("transactionType").toString();
            String sourceAccountNumber = request.get("sourceAccountNumber").toString();
            String targetAccountNumber = request.getOrDefault("targetAccountNumber", "N/A").toString();
            
            TransactionDTO transaction = transactionService.createTransaction(
                accountNumber, 
                amount, 
                transactionType, 
                sourceAccountNumber, 
                targetAccountNumber
            );
            
            return ResponseEntity.status(HttpStatus.CREATED).body(transaction);
        } catch (Exception e) {
            System.err.println("TransactionController: Error creating transaction - " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}