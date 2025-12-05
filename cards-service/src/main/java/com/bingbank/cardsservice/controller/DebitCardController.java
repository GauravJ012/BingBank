package com.bingbank.cardsservice.controller;

import com.bingbank.cardsservice.dto.ChangePinRequest;
import com.bingbank.cardsservice.dto.DeactivateCardRequest;
import com.bingbank.cardsservice.dto.DebitCardDTO;
import com.bingbank.cardsservice.service.DebitCardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/cards/debit")
public class DebitCardController {

    @Autowired
    private DebitCardService debitCardService;

    /**
     * Get debit card by customer ID
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getDebitCard(@PathVariable Long customerId) {
        try {
            System.out.println("DebitCardController: Fetching debit card for customer: " + customerId);
            DebitCardDTO card = debitCardService.getDebitCardByCustomerId(customerId);
            
            if (card == null) {
                // Return a response indicating no card found
                Map<String, Object> response = new HashMap<>();
                response.put("found", false);
                response.put("message", "No debit card found for customer");
                return ResponseEntity.ok(response);
            }
            
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            System.err.println("DebitCardController: Error - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Change ATM PIN
     */
    @PostMapping("/change-pin")
    public ResponseEntity<?> changeAtmPin(
            @Valid @RequestBody ChangePinRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("DebitCardController: Changing ATM PIN");
            DebitCardDTO card = debitCardService.changeAtmPin(request, authHeader);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "ATM PIN changed successfully",
                    "card", card
            ));
        } catch (Exception e) {
            System.err.println("DebitCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deactivate debit card
     */
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateCard(
            @RequestBody DeactivateCardRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("DebitCardController: Deactivating debit card");
            DebitCardDTO card = debitCardService.deactivateCard(request, authHeader);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Debit card deactivated successfully",
                    "card", card
            ));
        } catch (Exception e) {
            System.err.println("DebitCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}