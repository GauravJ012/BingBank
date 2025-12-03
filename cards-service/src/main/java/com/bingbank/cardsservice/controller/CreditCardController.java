package com.bingbank.cardsservice.controller;

import com.bingbank.cardsservice.dto.*;
import com.bingbank.cardsservice.model.CreditCard;
import com.bingbank.cardsservice.repository.CreditCardRepository;
import com.bingbank.cardsservice.service.CreditCardPdfService;
import com.bingbank.cardsservice.service.CreditCardService;
import com.itextpdf.text.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards/credit")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    @Autowired
    private CreditCardPdfService pdfService;

    @Autowired
    private CreditCardRepository creditCardRepository;

    /**
     * Get credit card by customer ID
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCreditCard(@PathVariable Long customerId) {
        try {
            System.out.println("CreditCardController: Fetching credit card for customer: " + customerId);
            CreditCardDTO card = creditCardService.getCreditCardByCustomerId(customerId);
            return ResponseEntity.ok(card);
        } catch (Exception e) {
            System.err.println("CreditCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get all transactions for a credit card
     */
    @GetMapping("/{cardId}/transactions")
    public ResponseEntity<?> getAllTransactions(@PathVariable Long cardId) {
        try {
            System.out.println("CreditCardController: Fetching all transactions for card: " + cardId);
            List<CreditCardTransactionDTO> transactions = creditCardService.getAllTransactions(cardId);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("CreditCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get filtered transactions
     */
    @PostMapping("/{cardId}/transactions/filter")
    public ResponseEntity<?> getFilteredTransactions(
            @PathVariable Long cardId,
            @RequestBody TransactionFilterRequest filterRequest) {
        try {
            filterRequest.setCardId(cardId);
            System.out.println("CreditCardController: Fetching filtered transactions");
            List<CreditCardTransactionDTO> transactions = creditCardService.getFilteredTransactions(filterRequest);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("CreditCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get monthly transactions
     */
    @GetMapping("/{cardId}/transactions/monthly/{year}/{month}")
    public ResponseEntity<?> getMonthlyTransactions(
            @PathVariable Long cardId,
            @PathVariable int year,
            @PathVariable int month) {
        try {
            System.out.println("CreditCardController: Fetching monthly transactions for " + year + "-" + month);
            List<CreditCardTransactionDTO> transactions = 
                    creditCardService.getMonthlyTransactions(cardId, year, month);
            return ResponseEntity.ok(transactions);
        } catch (Exception e) {
            System.err.println("CreditCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Download monthly statement PDF
     */
    @GetMapping("/{cardId}/statement/{year}/{month}")
    public ResponseEntity<?> downloadStatement(
            @PathVariable Long cardId,
            @PathVariable int year,
            @PathVariable int month,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("CreditCardController: Generating statement for " + year + "-" + month);
            
            // Get credit card first to get customer ID
            CreditCard card = creditCardRepository.findById(cardId)
                    .orElseThrow(() -> new RuntimeException("Credit card not found"));
            
            // Get credit card details
            CreditCardDTO creditCard = creditCardService.getCreditCardByCustomerId(card.getCustomerId());
            
            // Get monthly transactions
            List<CreditCardTransactionDTO> transactions = 
                    creditCardService.getMonthlyTransactions(cardId, year, month);
            
            // Generate PDF
            byte[] pdfBytes = pdfService.generateCreditCardStatement(creditCard, transactions, year, month);
            
            // Set headers for PDF download
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            String filename = "credit_card_statement_" + year + "_" + 
                    String.format("%02d", month) + ".pdf";
            headers.setContentDispositionFormData("attachment", filename);
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
            
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
            
        } catch (DocumentException | IOException e) {
            System.err.println("CreditCardController: Error generating PDF - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to generate statement"));
        } catch (Exception e) {
            System.err.println("CreditCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Pay credit card bill
     */
    @PostMapping("/pay-bill")
    public ResponseEntity<?> payBill(
            @Valid @RequestBody PayBillRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("CreditCardController: Processing bill payment");
            CreditCardDTO card = creditCardService.payBill(request, authHeader);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Payment processed successfully",
                    "card", card
            ));
        } catch (Exception e) {
            System.err.println("CreditCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Deactivate credit card
     */
    @PostMapping("/deactivate")
    public ResponseEntity<?> deactivateCard(
            @RequestBody DeactivateCardRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("CreditCardController: Deactivating credit card");
            CreditCardDTO card = creditCardService.deactivateCard(request, authHeader);
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Credit card deactivated successfully",
                    "card", card
            ));
        } catch (Exception e) {
            System.err.println("CreditCardController: Error - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}