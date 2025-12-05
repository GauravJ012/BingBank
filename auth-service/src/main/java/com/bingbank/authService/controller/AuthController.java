package com.bingbank.authService.controller;

import com.bingbank.authService.dto.CustomerDTO;
import com.bingbank.authService.dto.Enable2FARequest;
import com.bingbank.authService.dto.LoginRequest;
import com.bingbank.authService.dto.RegisterRequest;
import com.bingbank.authService.dto.VerifyOTPRequest;
import com.bingbank.authService.model.Customer;
import com.bingbank.authService.service.AuthService;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
//@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOTP(@Valid @RequestBody VerifyOTPRequest verifyOTPRequest) {
        return authService.verifyOTP(verifyOTPRequest);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return authService.register(registerRequest);
    }

    @PostMapping("/enable-2fa/{customerId}")
    public ResponseEntity<?> enable2FA(@PathVariable Long customerId, @RequestBody boolean enable) {
        return authService.enable2FA(customerId, enable);
    }
    
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<?> getCustomerDetails(@PathVariable Long customerId) {
        return authService.getCustomerDetails(customerId);
    }
    
    /**
     * Verify registration OTP
     */
    @PostMapping("/verify-registration-otp")
    public ResponseEntity<?> verifyRegistrationOTP(@RequestBody VerifyOTPRequest request) {
        try {
            System.out.println("AuthController: Verifying registration OTP for email: " + request.getEmail());
            return authService.verifyRegistrationOTP(request);
        } catch (Exception e) {
            System.err.println("AuthController: Error verifying registration OTP - " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration verification failed: " + e.getMessage());
        }
    }
}