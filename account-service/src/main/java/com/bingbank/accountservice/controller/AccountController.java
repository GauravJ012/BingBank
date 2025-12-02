package com.bingbank.accountservice.controller;

import com.bingbank.accountservice.dto.AccountDTO;
import com.bingbank.accountservice.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}