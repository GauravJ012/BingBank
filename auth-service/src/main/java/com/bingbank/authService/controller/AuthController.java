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
    
    
    /**
     * Request password reset OTP
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<?> requestPasswordReset(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            System.out.println("AuthController: Password reset requested for email: " + email);
            
            authService.requestPasswordReset(email);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "OTP has been sent to your email");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("AuthController: Error requesting password reset - " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Verify password reset OTP
     */
    @PostMapping("/verify-reset-otp")
    public ResponseEntity<?> verifyPasswordResetOTP(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            System.out.println("AuthController: Verifying password reset OTP for email: " + email);
            
            boolean isValid = authService.verifyPasswordResetOTP(email, otp);
            
            if (isValid) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("message", "OTP verified successfully");
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest()
                        .body(Map.of("success", false, "message", "Invalid or expired OTP"));
            }
        } catch (Exception e) {
            System.err.println("AuthController: Error verifying reset OTP - " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    /**
     * Reset password
     */
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String otp = request.get("otp");
            String newPassword = request.get("newPassword");
            System.out.println("AuthController: Resetting password for email: " + email);
            
            authService.resetPassword(email, otp, newPassword);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Password reset successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("AuthController: Error resetting password - " + e.getMessage());
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
    
}