package com.bingbank.authService.service;

import com.bingbank.authService.client.AccountServiceClient;
import com.bingbank.authService.dto.CustomerDTO;
import com.bingbank.authService.dto.JwtAuthResponse;
import com.bingbank.authService.dto.LoginRequest;
import com.bingbank.authService.dto.RegisterRequest;
import com.bingbank.authService.dto.VerifyOTPRequest;
import com.bingbank.authService.model.Customer;
import com.bingbank.authService.model.OTP;
import com.bingbank.authService.model.PendingRegistration;
import com.bingbank.authService.repository.CustomerRepository;
import com.bingbank.authService.repository.OTPRepository;
import com.bingbank.authService.repository.PendingRegistrationRepository;
import com.bingbank.authService.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {

    private static final String LOGIN_PURPOSE = "LOGIN";
    private static final String REGISTRATION_PURPOSE = "REGISTRATION";

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private PendingRegistrationRepository pendingRegistrationRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private OTPService otpService;
    
    @Autowired
    private AccountServiceClient accountServiceClient;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private OTPRepository otpRepository;

    public ResponseEntity<?> login(LoginRequest loginRequest) {
        try {
            // Authenticate with username and password
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Get customer details
            Customer customer = customerRepository.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if 2FA is enabled
            if (customer.getTwoFactorEnabled()) {
                // Generate and send OTP
                try {
                    otpService.generateAndSendOTP(customer, LOGIN_PURPOSE);
                    
                    // Return response indicating OTP is required
                    return ResponseEntity.ok(JwtAuthResponse.builder()
                            .otpRequired(true)
                            .email(customer.getEmail())
                            .customerId(customer.getCustomerId())
                            .firstName(customer.getFirstName())
                            .lastName(customer.getLastName())
                            .build());
                } catch (MessagingException e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                            .body("Failed to send OTP email");
                }
            } else {
                // 2FA not enabled, generate token
                String token = tokenProvider.generateToken(authentication);

                return ResponseEntity.ok(JwtAuthResponse.builder()
                        .accessToken(token)
                        .tokenType("Bearer")
                        .email(customer.getEmail())
                        .customerId(customer.getCustomerId())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .otpRequired(false)
                        .build());
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid email or password");
        }
    }

    public ResponseEntity<?> verifyOTP(VerifyOTPRequest verifyOTPRequest) {
        try {
            // Find customer by email
            Customer customer = customerRepository.findByEmail(verifyOTPRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Validate OTP
            boolean isValid = otpService.validateOTP(customer, verifyOTPRequest.getOtp(), LOGIN_PURPOSE);

            if (isValid) {
                // Generate JWT token
                String token = tokenProvider.generateToken(customer.getEmail());

                return ResponseEntity.ok(JwtAuthResponse.builder()
                        .accessToken(token)
                        .tokenType("Bearer")
                        .email(customer.getEmail())
                        .customerId(customer.getCustomerId())
                        .firstName(customer.getFirstName())
                        .lastName(customer.getLastName())
                        .otpRequired(false)
                        .build());
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Invalid or expired OTP");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error verifying OTP: " + e.getMessage());
        }
    }

    public ResponseEntity<?> register(RegisterRequest registerRequest) {
        try {
            // Check if email exists
            if (customerRepository.existsByEmail(registerRequest.getEmail())) {
                return ResponseEntity.badRequest().body("Email already exists");
            }

            // Check if account number exists and is valid
            if (!accountServiceClient.accountExists(registerRequest.getAccountNumber())) {
                return ResponseEntity.badRequest()
                        .body("Invalid account number. Please contact customer support.");
            }

            // Generate OTP for registration verification
            String otp = generateOTP();
            
            // Encode password before storing
            String encodedPassword = passwordEncoder.encode(registerRequest.getPassword());
            
            // Create pending registration
            PendingRegistration pendingRegistration = new PendingRegistration();
            pendingRegistration.setEmail(registerRequest.getEmail());
            pendingRegistration.setFirstName(registerRequest.getFirstName());
            pendingRegistration.setLastName(registerRequest.getLastName());
            pendingRegistration.setAge(registerRequest.getAge());
            pendingRegistration.setGender(registerRequest.getGender());
            pendingRegistration.setAddress(registerRequest.getAddress());
            pendingRegistration.setMobile(registerRequest.getMobile());
            pendingRegistration.setTwoFactorEnabled(registerRequest.getTwoFactorEnabled());
            pendingRegistration.setPassword(encodedPassword);
            pendingRegistration.setAccountNumber(registerRequest.getAccountNumber());
            pendingRegistration.setOtpCode(otp);
            pendingRegistration.setExpiresAt(LocalDateTime.now().plusMinutes(5));
            
            // Delete any existing pending registration for this email
            pendingRegistrationRepository.findByEmail(registerRequest.getEmail())
                    .ifPresent(existing -> pendingRegistrationRepository.delete(existing));
            
            // Save pending registration
            pendingRegistrationRepository.save(pendingRegistration);
            
            // Send OTP email
            try {
                emailService.sendOtpEmail(registerRequest.getEmail(), otp);
            } catch (MessagingException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("Failed to send OTP email. Please try again.");
            }
            
            return ResponseEntity.ok()
                    .body(Map.of(
                        "message", "OTP has been sent to your email address for verification.",
                        "email", registerRequest.getEmail()
                    ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration failed: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<?> verifyRegistrationOTP(VerifyOTPRequest request) {
        try {
            // Get pending registration
            PendingRegistration pendingRegistration = pendingRegistrationRepository
                    .findByEmailAndOtpCodeAndExpiresAtGreaterThan(
                            request.getEmail(), 
                            request.getOtp(), 
                            LocalDateTime.now()
                    )
                    .orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));
            
            // Create new customer
            Customer customer = new Customer();
            customer.setFirstName(pendingRegistration.getFirstName());
            customer.setLastName(pendingRegistration.getLastName());
            customer.setEmail(pendingRegistration.getEmail());
            customer.setPassword(pendingRegistration.getPassword()); // Already encoded
            customer.setAge(pendingRegistration.getAge());
            customer.setGender(pendingRegistration.getGender());
            customer.setAddress(pendingRegistration.getAddress());
            customer.setMobile(pendingRegistration.getMobile());
            customer.setTwoFactorEnabled(pendingRegistration.getTwoFactorEnabled());
            customer.setAccountNumber(pendingRegistration.getAccountNumber());

            customerRepository.save(customer);
            
            // Delete pending registration
            pendingRegistrationRepository.delete(pendingRegistration);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body("Customer registered successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Registration verification failed: " + e.getMessage());
        }
    }

    public ResponseEntity<?> enable2FA(Long customerId, boolean enable) {
        try {
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found"));
            
            customer.setTwoFactorEnabled(enable);
            customerRepository.save(customer);
            
            return ResponseEntity.ok("Two-factor authentication " + 
                    (enable ? "enabled" : "disabled") + " successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating 2FA settings: " + e.getMessage());
        }
    }
    
    public ResponseEntity<?> getCustomerDetails(Long customerId) {
        try {
            // Find customer by ID
            Customer customer = customerRepository.findById(customerId)
                    .orElseThrow(() -> new RuntimeException("Customer not found with ID: " + customerId));
            
            // Convert to DTO
            CustomerDTO customerDTO = new CustomerDTO();
            customerDTO.setId(customer.getCustomerId());
            customerDTO.setFirstName(customer.getFirstName());
            customerDTO.setLastName(customer.getLastName());
            customerDTO.setEmail(customer.getEmail());
            customerDTO.setMobile(customer.getMobile());
            customerDTO.setAddress(customer.getAddress());
            customerDTO.setAge(customer.getAge());
            customerDTO.setGender(customer.getGender());
            customerDTO.setTwoFactorEnabled(customer.getTwoFactorEnabled());
            customerDTO.setAccountNumber(customer.getAccountNumber()); // Include account number
            
            return ResponseEntity.ok(customerDTO);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error retrieving customer details: " + e.getMessage());
        }
    }

    /**
     * Request password reset using OTP table
     */
    @Transactional
    public void requestPasswordReset(String email) {
        System.out.println("AuthService: Requesting password reset for email: " + email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email"));
        
        // Generate OTP
        String otpCode = generateOTP();
        
        // Create new OTP entry
        OTP otp = new OTP();
        otp.setCustomer(customer);
        otp.setOtpCode(otpCode);
        otp.setPurpose("PASSWORD_RESET");
        otp.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otp.setVerified(false);
        
        otpRepository.save(otp);
        
        // Send OTP email
        emailService.sendPasswordResetOTP(email, otpCode, customer.getFirstName());
        
        System.out.println("AuthService: Password reset OTP sent successfully");
    }

    /**
     * Verify password reset OTP using OTP table
     */
    public boolean verifyPasswordResetOTP(String email, String otpCode) {
        System.out.println("AuthService: Verifying password reset OTP for email: " + email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        OTP otp = otpRepository.findByCustomerAndOtpCodeAndPurposeAndVerifiedFalseAndExpiresAtGreaterThan(
                customer,
                otpCode,
                "PASSWORD_RESET",
                LocalDateTime.now()
        ).orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));
        
        // Mark OTP as verified (but don't delete it yet - we need it for password reset)
        otp.setVerified(true);
        otpRepository.save(otp);
        
        System.out.println("AuthService: Password reset OTP verified successfully");
        return true;
    }

    /**
     * Reset password using OTP table
     */
    @Transactional
    public void resetPassword(String email, String otpCode, String newPassword) {
        System.out.println("AuthService: Resetting password for email: " + email);
        
        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        
        // Find the verified OTP (verified = true means it passed step 2)
        OTP otp = otpRepository.findByCustomerAndOtpCodeAndPurposeAndExpiresAtGreaterThan(
                customer,
                otpCode,
                "PASSWORD_RESET",
                LocalDateTime.now()
        ).orElseThrow(() -> new RuntimeException("Invalid or expired OTP"));
        
        // Check if OTP was verified in previous step
        if (!otp.getVerified()) {
            throw new RuntimeException("OTP not verified. Please verify OTP first.");
        }
        
        // Encode new password
        String encodedPassword = passwordEncoder.encode(newPassword);
        customer.setPassword(encodedPassword);
        customerRepository.save(customer);
        
        // Delete the used OTP
        otpRepository.delete(otp);
        
        System.out.println("AuthService: Password reset successfully");
    }
    
    // Helper method to generate OTP
    private String generateOTP() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
   
}