package com.bingbank.fundtransferservice.controller;

import com.bingbank.fundtransferservice.dto.TransferRequest;
import com.bingbank.fundtransferservice.dto.TransferResponse;
import com.bingbank.fundtransferservice.service.FundTransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/fund-transfer")
public class FundTransferController {

    @Autowired
    private FundTransferService fundTransferService;

    /**
     * Initiate a fund transfer
     */
    @PostMapping("/transfer")
    public ResponseEntity<?> initiateTransfer(
            @Valid @RequestBody TransferRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("FundTransferController: Transfer request received from customer: " + request.getCustomerId());
            TransferResponse response = fundTransferService.initiateTransfer(request, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            System.err.println("FundTransferController: Error processing transfer - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Get transfer history for a customer
     */
    @GetMapping("/history/{customerId}")
    public ResponseEntity<List<TransferResponse>> getTransferHistory(@PathVariable Long customerId) {
        System.out.println("FundTransferController: Fetching transfer history for customer: " + customerId);
        List<TransferResponse> history = fundTransferService.getTransferHistory(customerId);
        return ResponseEntity.ok(history);
    }

    /**
     * Get transfer by ID
     */
    @GetMapping("/{transferId}")
    public ResponseEntity<?> getTransferById(@PathVariable Long transferId) {
        try {
            System.out.println("FundTransferController: Fetching transfer: " + transferId);
            TransferResponse transfer = fundTransferService.getTransferById(transferId);
            return ResponseEntity.ok(transfer);
        } catch (Exception e) {
            System.err.println("FundTransferController: Error fetching transfer - " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}