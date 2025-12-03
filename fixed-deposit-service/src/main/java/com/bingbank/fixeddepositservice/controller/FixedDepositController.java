package com.bingbank.fixeddepositservice.controller;

import com.bingbank.fixeddepositservice.dto.*;
import com.bingbank.fixeddepositservice.service.FixedDepositService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/fixed-deposits")
public class FixedDepositController {

    @Autowired
    private FixedDepositService fdService;

    /**
     * Get all active FDs for a customer
     */
    @GetMapping("/customer/{customerId}/active")
    public ResponseEntity<List<FixedDepositDTO>> getActiveFDs(@PathVariable Long customerId) {
        System.out.println("FDController: Fetching active FDs for customer: " + customerId);
        List<FixedDepositDTO> fds = fdService.getActiveFDs(customerId);
        return ResponseEntity.ok(fds);
    }

    /**
     * Get all FDs (active and closed) for a customer
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<FixedDepositDTO>> getAllFDs(@PathVariable Long customerId) {
        System.out.println("FDController: Fetching all FDs for customer: " + customerId);
        List<FixedDepositDTO> fds = fdService.getAllFDs(customerId);
        return ResponseEntity.ok(fds);
    }

    /**
     * Create a new FD
     */
    @PostMapping("/create")
    public ResponseEntity<?> createFD(
            @Valid @RequestBody CreateFDRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("FDController: Creating FD for customer: " + request.getCustomerId());
            FixedDepositDTO fd = fdService.createFD(request, authHeader);
            return ResponseEntity.status(HttpStatus.CREATED).body(fd);
        } catch (Exception e) {
            System.err.println("Error creating FD: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Close an existing FD
     */
    @PostMapping("/close")
    public ResponseEntity<?> closeFD(
            @RequestBody CloseFDRequest request,
            @RequestHeader("Authorization") String authHeader) {
        try {
            System.out.println("FDController: Closing FD: " + request.getFdId());
            FixedDepositDTO fd = fdService.closeFD(request, authHeader);
            return ResponseEntity.ok(fd);
        } catch (Exception e) {
            System.err.println("Error closing FD: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Get growth data for FD tracking
     */
    @GetMapping("/{fdId}/growth")
    public ResponseEntity<GrowthDataDTO> getGrowthData(@PathVariable Long fdId) {
        System.out.println("FDController: Fetching growth data for FD: " + fdId);
        GrowthDataDTO growthData = fdService.getGrowthData(fdId);
        return ResponseEntity.ok(growthData);
    }
}