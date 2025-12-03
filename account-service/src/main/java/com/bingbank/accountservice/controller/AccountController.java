package com.bingbank.accountservice.controller;

import com.bingbank.accountservice.dto.AccountDTO;
import com.bingbank.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Autowired
    private AccountService accountService;

    /**
     * Get all accounts for a customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountDTO>> getAccountsByCustomerId(@PathVariable Long customerId) {
        System.out.println("AccountController: Fetching accounts for customer ID: " + customerId);
        List<AccountDTO> accounts = accountService.getAccountsByCustomerId(customerId);
        System.out.println("AccountController: Found " + accounts.size() + " accounts");
        return ResponseEntity.ok(accounts);
    }

    /**
     * Get account by account number
     */
    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountDTO> getAccountByAccountNumber(@PathVariable String accountNumber) {
        System.out.println("AccountController: Fetching account: " + accountNumber);
        AccountDTO account = accountService.getAccountByAccountNumber(accountNumber);
        return ResponseEntity.ok(account);
    }

    /**
     * Check if account exists (public endpoint for registration)
     */
    @GetMapping("/exists/{accountNumber}")
    public ResponseEntity<Map<String, Boolean>> checkAccountExists(@PathVariable String accountNumber) {
        System.out.println("AccountController: Checking if account exists: " + accountNumber);
        boolean exists = accountService.accountExists(accountNumber);
        Map<String, Boolean> response = new HashMap<>();
        response.put("exists", exists);
        return ResponseEntity.ok(response);
    }

    /**
     * Get current user info (for testing JWT)
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, String>> getCurrentUser(
            @RequestHeader(value = "X-User-Id", required = false) String userId) {
        Map<String, String> response = new HashMap<>();
        response.put("userId", userId != null ? userId : "No user ID in header");
        return ResponseEntity.ok(response);
    }

    /**
     * Debit amount from account
     */
    @PostMapping("/{accountNumber}/debit")
    public ResponseEntity<?> debitAccount(
            @PathVariable String accountNumber,
            @RequestBody Map<String, Object> request) {
        try {
            System.out.println("AccountController: Debit request for account: " + accountNumber);
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = request.getOrDefault("description", "Debit").toString();
            
            AccountDTO updatedAccount = accountService.debitFromAccount(accountNumber, amount, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newBalance", updatedAccount.getBalance());
            response.put("message", "Amount debited successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("AccountController: Error debiting account - " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.err.println("AccountController: Unexpected error - " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    /**
     * Credit amount to account
     */
    @PostMapping("/{accountNumber}/credit")
    public ResponseEntity<?> creditAccount(
            @PathVariable String accountNumber,
            @RequestBody Map<String, Object> request) {
        try {
            System.out.println("AccountController: Credit request for account: " + accountNumber);
            
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String description = request.getOrDefault("description", "Credit").toString();
            
            AccountDTO updatedAccount = accountService.creditToAccount(accountNumber, amount, description);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("newBalance", updatedAccount.getBalance());
            response.put("message", "Amount credited successfully");
            
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("AccountController: Error crediting account - " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            System.err.println("AccountController: Unexpected error - " + e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("error", "Internal server error");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}